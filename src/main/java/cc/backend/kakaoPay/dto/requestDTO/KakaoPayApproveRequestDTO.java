package cc.backend.kakaoPay.dto.requestDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveRequestDTO {
    private String cid; // 가맹점 코드 (지금은 테스트코드)

    private String tid; // 결제 고유번호

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 가맹점 주문번호 (ticketId)

    @JsonProperty("partner_user_id")
    private String partnerUserId; // 가맹점 회원 id (memberId)

    @JsonProperty("pg_token")
    private String pgToken; // 결제승인 요청을 인증하는 토큰
}
