package cc.backend.kakaoPay.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.dto.requestDTO.KakaoPayReadyRequestDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

    private final WebClient kakaoWebClient;
    private final MemberTicketRepository memberTicketRepository;
    private final KakaoPayBusinessService kakaoPayBusinessService;

    @Value("${kakaopay.cid}")
    private String cid;

    public Mono<KakaoPayReadyResponseDTO> ready(Long ticketId, String partnerUserId) {
        MemberTicket ticket = memberTicketRepository.findById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        String itemName = ticket.getAmateurTicket().getDiscountName() + " - " +
                ticket.getAmateurTicket().getAmateurShow().getName();

        KakaoPayReadyRequestDTO request = KakaoPayReadyRequestDTO.builder()
                .cid(cid)
                .partnerOrderId(String.valueOf(ticketId))
                .partnerUserId(partnerUserId)
                .itemName(itemName)
                .quantity(ticket.getQuantity())
                .totalAmount(ticket.getTotalPrice())
                .taxFreeAmount(0)
                .approvalUrl("http://localhost:8080/kakaoPay/success?orderId=" + ticketId)
                .cancelUrl("http://localhost:8080/kakaoPay/cancel")
                .failUrl("http://localhost:8080/kakaoPay/fail")
                .build();

        // JSON 확인용 로그 출력
        try {
            System.out.println(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return kakaoWebClient.post()
                .uri("/online/v1/payment/ready")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            System.out.println("KakaoPay error response: " + errorBody);
                            return Mono.error(new RuntimeException("KakaoPay API error: " + errorBody));
                        })
                )
                .bodyToMono(KakaoPayReadyResponseDTO.class);
    }

    public Mono<KakaoPayApproveResponseDTO> approve(String tid, String partnerOrderId, String partnerUserId, String pgToken) {
        return kakaoWebClient.post()
                .uri("/online/v1/payment/approve")
                .bodyValue(new HashMap<>() {{
                    put("cid", cid);
                    put("tid", tid);
                    put("partner_order_id", partnerOrderId);
                    put("partner_user_id", partnerUserId);
                    put("pg_token", pgToken);
                }})
                .retrieve()
                .bodyToMono(KakaoPayApproveResponseDTO.class)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(response -> kakaoPayBusinessService.handleApprovedTicket(tid, partnerOrderId));
    }
}
