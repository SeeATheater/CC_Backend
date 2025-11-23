package cc.backend.kakaoPay.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayApproveRequestDTO;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayCancelRequestDTO;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayReadyRequestDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayCancelResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class KakaoPayService {

    private final WebClient kakaoWebClient;
    private final MemberTicketRepository memberTicketRepository;

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.url.approval}")
    private String approvalUrl;

    @Value("${kakaopay.url.cancel}")
    private String cancelUrl;

    @Value("${kakaopay.url.fail}")
    private String failUrl;

    public KakaoPayReadyResponseDTO ready(Long memberTicketId, String partnerUserId) {

        MemberTicket memberTicket = memberTicketRepository.findWithTicketAndShowById(memberTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 재고 상태 검증
        if (memberTicket.getReservationStatus() != ReservationStatus.PENDING) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STATUS_INVALID);
        }

        // itemName (할인명 - 공연이름)
        String itemName = memberTicket.getAmateurTicket().getDiscountName() + " - " +
                memberTicket.getAmateurTicket().getAmateurShow().getName();

        KakaoPayReadyRequestDTO requestDTO = KakaoPayReadyRequestDTO.builder()
                .cid(cid)
                .partnerOrderId(String.valueOf(memberTicketId))
                .partnerUserId(partnerUserId)
                .itemName(itemName)
                .quantity(memberTicket.getQuantity())
                .totalAmount(memberTicket.getTotalPrice())
                .taxFreeAmount(0)
                .approvalUrl(approvalUrl + "?partner_order_id=" + memberTicketId)
                .cancelUrl(cancelUrl + "?partner_order_id=" + memberTicketId)
                .failUrl(failUrl + "?partner_order_id=" + memberTicketId)
                .build();

        // post 요청 (ready)
        return kakaoWebClient.post()
                .uri("/online/v1/payment/ready")
                .bodyValue(requestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("KakaoPay ready API 에러: {}", errorBody);
                            return Mono.error(new RuntimeException("KakaoPay API error: " + errorBody));
                        })
                )
                .bodyToMono(KakaoPayReadyResponseDTO.class)
                .block(); // 동기 처리
    }

    public KakaoPayApproveResponseDTO approve(String tid, String partnerOrderId, String partnerUserId, String pgToken) {

        // partnerOrderId로 MemberTicket 조회
        MemberTicket memberTicket = memberTicketRepository.findById(Long.valueOf(partnerOrderId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 티켓 상태가 EXPIRED 이면 승인 불가
        if (memberTicket.getReservationStatus().equals(ReservationStatus.EXPIRED)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_EXPIRED);
        }

        KakaoPayApproveRequestDTO requestDTO = KakaoPayApproveRequestDTO.builder()
                .cid(cid)
                .tid(tid)
                .partnerOrderId(partnerOrderId)
                .partnerUserId(partnerUserId)
                .pgToken(pgToken)
                .build();

        return kakaoWebClient.post()
                .uri("/online/v1/payment/approve")
                .bodyValue(requestDTO)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("KakaoPay approve API 에러: {}", errorBody);
                            return Mono.error(new RuntimeException("KakaoPay API error: " + errorBody));
                        })
                )
                .bodyToMono(KakaoPayApproveResponseDTO.class)
                .block();
    }

    public KakaoPayCancelResponseDTO cancel(String tid, Integer cancelAmount) {

        KakaoPayCancelRequestDTO requestDTO = KakaoPayCancelRequestDTO.builder()
             .cid(cid)
             .tid(tid)
             .cancelAmount(cancelAmount)
             .cancelTaxFreeAmount(0)
             .build();

        return kakaoWebClient.post()
            .uri("/online/v1/payment/cancel")
            .bodyValue(requestDTO)
            .retrieve()
            .onStatus(HttpStatusCode::isError, clientResponse ->
                clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                    log.error("KakaoPay cancel API 에러: {}", errorBody);
                    return Mono.error(new RuntimeException("KakaoPay API error: " + errorBody));
                })
            )
            .bodyToMono(KakaoPayCancelResponseDTO.class)
            .block();
    }
}
