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
public class KakaoPayReadyRequestDTO {
    private String cid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 이게 ticketId임

    @JsonProperty("partner_user_id")
    private String partnerUserId;

    @JsonProperty("item_name")
    private String itemName;

    private Integer quantity;

    @JsonProperty("total_amount")
    private Integer totalAmount;

    @JsonProperty("tax_free_amount")
    private Integer taxFreeAmount;

    @JsonProperty("approval_url")
    private String approvalUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

    @JsonProperty("fail_url")
    private String failUrl;
}
