package cc.backend.kakaoPay.dto.responseDTO;

import cc.backend.kakaoPay.dto.Amount;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveResponseDTO {

    private String aid; // 요청 고유 번호 - 승인/취소가 구분된 결제번호

    private String tid; // 결제 고유 번호 - 승인/취소가 동일한 결제번호

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 가맹점 주문번호

    @JsonProperty("partner_user_id")
    private String partnerUserId; // 가맹점 회원 id

    @JsonProperty("payment_method_type")
    private String paymentMethodType; // 결제 수단

    @JsonProperty("item_name")
    private String itemName; // 상품 이름

    private Amount amount; // 결제 금액 정보 -> 클래스 따로 뺌

    @JsonProperty("approved_at")
    private String approvedAt; // 결제 승인 시각
}
