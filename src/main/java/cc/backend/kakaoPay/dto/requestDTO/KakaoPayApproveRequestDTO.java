package cc.backend.kakaoPay.dto.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveRequestDTO {
    private String tid; // 결제 고유번호
    private String partnerOrderId; // 가맹점 주문번호 (MemberTicket.id)
    private String partnerUserId; // 가맹점 회원 id (회원)
    private String pgToken; // 결제승인 요청을 인증하는 토큰
}
