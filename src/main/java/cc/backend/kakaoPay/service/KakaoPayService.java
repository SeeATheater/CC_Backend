package cc.backend.kakaoPay.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayApproveRequestDTO;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayReadyRequestDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoPayService {

    private final WebClient kakaoWebClient;
    private final MemberTicketRepository memberTicketRepository;
    private final KakaoPayBusinessService kakaoPayBusinessService;

    @Value("${kakaopay.cid}")
    private String cid;

    public Mono<KakaoPayReadyResponseDTO> ready(Long ticketId, String partnerUserId) {

        return Mono.fromCallable(() -> memberTicketRepository.findWithTicketAndShowById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(memberTicket -> {

                    // itemName (할인명 - 공연이름)
                    String itemName = memberTicket.getAmateurTicket().getDiscountName() + " - " +
                            memberTicket.getAmateurTicket().getAmateurShow().getName();

                    KakaoPayReadyRequestDTO requestDTO = KakaoPayReadyRequestDTO.builder()
                            .cid(cid)
                            .partnerOrderId(String.valueOf(ticketId))
                            .partnerUserId(partnerUserId)
                            .itemName(itemName)
                            .quantity(memberTicket.getQuantity())
                            .totalAmount(memberTicket.getTotalPrice())
                            .taxFreeAmount(0)
                            .approvalUrl("http://localhost:8080/kakaoPay/approve?partner_order_id=" + ticketId)
                            .cancelUrl("http://localhost:8080/kakaoPay/cancel")
                            .failUrl("http://localhost:8080/kakaoPay/fail")
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
                            .bodyToMono(KakaoPayReadyResponseDTO.class) // Mono로 감싸 응답을 dto로 변환
                            .flatMap(readyResponse -> {
                                memberTicket.updateTid(readyResponse.getTid());

                                return Mono.fromCallable(() -> memberTicketRepository.save(memberTicket))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .thenReturn(readyResponse); // 원래 결과 다시 리턴
                            });
                });
    }

    public Mono<KakaoPayApproveResponseDTO> approve(String partnerOrderId, String pgToken) {

        // partnerOrderId로 MemberTicket 조회
        return Mono.fromCallable(() -> memberTicketRepository.findById(Long.valueOf(partnerOrderId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND)))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(memberTicket -> {
                    if (memberTicket.getReservationStatus().equals(ReservationStatus.RESERVED)) {
                        return Mono.error(new GeneralException(ErrorStatus.MEMBER_TICKET_ALREADY_RESERVED));
                    }

                    // 저장된 tid 가져오기
                    String tid = memberTicket.getKakaoTid();
                    if (tid == null) {
                        return Mono.error(new GeneralException(ErrorStatus.MEMBER_TICKET_TID_NOT_FOUND));
                    }

                    String partnerUserId = memberTicket.getMember().getId().toString(); // 멤버를 DB에서 추적하기

                    KakaoPayApproveRequestDTO requestDTO = new KakaoPayApproveRequestDTO(
                            cid, tid, partnerOrderId, partnerUserId, pgToken
                    );

                    return kakaoWebClient.post()
                            .uri("/online/v1/payment/approve")
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(requestDTO)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, clientResponse ->
                                    clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                        log.error("KakaoPay approve API 에러: {}", errorBody);
                                        return Mono.error(new RuntimeException("KakaoPay API error: " + errorBody));
                                    })
                            )
                            .bodyToMono(KakaoPayApproveResponseDTO.class)
                            .flatMap(response ->
                                    Mono.fromRunnable(() -> kakaoPayBusinessService.handleApprovedTicket(tid, partnerOrderId))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .thenReturn(response)
                            );
                });
    }
}
