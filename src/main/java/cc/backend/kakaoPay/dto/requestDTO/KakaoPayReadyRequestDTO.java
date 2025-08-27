package cc.backend.kakaoPay.dto.requestDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoPayReadyRequestDTO { // required 만 담음
    private String cid; // 가맹점 코드 (지금은 테스트코드)

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 이게 ticketId임

    @JsonProperty("partner_user_id")
    private String partnerUserId; // memberId

    @JsonProperty("item_name")
    private String itemName; // 상품명 (할인명 + 공연이름)

    private Integer quantity; // 상품 수량

    @JsonProperty("total_amount")
    private Integer totalAmount; // 상품 총액

    @JsonProperty("tax_free_amount")
    private Integer taxFreeAmount; // 상품 비과세 금액

    @JsonProperty("approval_url")
    private String approvalUrl; // 결제 성공 시 redirect url

    @JsonProperty("cancel_url")
    private String cancelUrl; // 결제 취소 시 redirect url

    @JsonProperty("fail_url")
    private String failUrl; // 결제 실패 시 redirect url
}
