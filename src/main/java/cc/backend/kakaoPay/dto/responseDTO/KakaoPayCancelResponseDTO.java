package cc.backend.kakaoPay.dto.responseDTO;

import cc.backend.kakaoPay.dto.Amount;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoPayCancelResponseDTO {

    private String aid; // 요청 고유 번호

    private String tid; // 결제 고유 번호

    private String cid; // 가맹점 코드

    private String status; // 결제 상태

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 가맹점 주문번호

    @JsonProperty("partner_user_id")
    private String partnerUserId; // 가맹점 회원 id

    @JsonProperty("payment_method_type")
    private String paymentMethodType; // 결제 수단 (card / money)

    private Amount amount; // 결제 금액

    @JsonProperty("approved_cancel_amount")
    private Amount approvedCancelAmount; // 이번 요청으로 취소된 금액

    @JsonProperty("canceled_amount")
    private Amount canceledAmount; // 누계 취소 금액

    @JsonProperty("cancel_available_amount")
    private Amount cancelAvailableAmount; // 남은 취소 가능 금액

    @JsonProperty("item_name")
    private String itemName; // 상품 이름

    @JsonProperty("item_code")
    private String itemCode; // 상품 코드

    private Integer quantity; // 상품 수량

    @JsonProperty("created_at")
    private LocalDateTime createdAt; // 결제 준비 요청 시각

    @JsonProperty("approved_at")
    private LocalDateTime approvedAt; // 결제 승인 시각

    @JsonProperty("canceled_at")
    private LocalDateTime canceledAt; // 결제 취소 시각

    private String payload; // 취소 요청 시 전달한 값
}
