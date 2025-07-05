package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayApproveRequestDTO;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayReadyRequestDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoPayService {

    private final WebClient kakaoWebClient;
    private final MemberTicketRepository memberTicketRepository;
    private final KakaoPayBusinessService kakaoPayBusinessService;
    private final AmateurRoundsRepository amateurRoundsRepository;

    @Value("${kakaopay.cid}")
    private String cid;

    public KakaoPayReadyResponseDTO ready(Long ticketId, String partnerUserId) {

        MemberTicket memberTicket = memberTicketRepository.findWithTicketAndShowById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 현재 회차의 최신 재고 상태를 DB에서 직접 조회
        int currentStock = amateurRoundsRepository.findById(memberTicket.getAmateurRound().getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.ROUND_NOT_FOUND))
                .getTotalTicket();

        // 현재 재고와 요청한 수량 비교
        if (currentStock < memberTicket.getQuantity()) {
            // 재고가 부족하면 ready에서 예외 발생
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STOCK);
        }

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
        KakaoPayReadyResponseDTO readyResponse = kakaoWebClient.post()
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
                .block();

        if (readyResponse == null) {
            throw new RuntimeException("카카오페이 결제 준비 응답을 받지 못했습니다.");
        }

        memberTicket.updateTid(readyResponse.getTid()); // tid 디비에 저장
        memberTicketRepository.save(memberTicket);

        return readyResponse;
    }

    public KakaoPayApproveResponseDTO approve(String partnerOrderId, String pgToken) {

        // partnerOrderId로 MemberTicket 조회
        MemberTicket memberTicket = memberTicketRepository.findById(Long.valueOf(partnerOrderId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 저장된 tid 가져오기
        String tid = memberTicket.getKakaoTid();
        if (tid == null) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_TID_NOT_FOUND);
        }

        String partnerUserId = memberTicket.getMember().getId().toString(); // 멤버를 DB에서 추적하기

        KakaoPayApproveRequestDTO requestDTO = new KakaoPayApproveRequestDTO(
                cid, tid, partnerOrderId, partnerUserId, pgToken
        );

        KakaoPayApproveResponseDTO response = kakaoWebClient.post()
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

        if (response == null) {
            throw new RuntimeException("카카오페이 결제 승인 응답을 받지 못했습니다.");
        }

        // 예약 확정 (재고는 이미 ready에서 선점된 상태!)
        kakaoPayBusinessService.handleApprovedTicket(partnerOrderId);

        return response;
    }
}
