package cc.backend.kakaoPay.dto.responseDTO;

import cc.backend.kakaoPay.dto.Amount;
import cc.backend.kakaoPay.dto.CardInfo;
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

    private String cid; // 가맹점 코드

    private String sid; // 정기 결제용 ID (정기 결제 CID로 단건 결제 요청 시 발급)

    @JsonProperty("partner_order_id")
    private String partnerOrderId; // 가맹점 주문번호

    @JsonProperty("partner_user_id")
    private String partnerUserId; // 가맹점 회원 id

    @JsonProperty("payment_method_type")
    private String paymentMethodType; // 결제 수단 (card / money)

    @JsonProperty("item_name")
    private String itemName; // 상품 이름

    @JsonProperty("item_code")
    private String itemCode; // 상품 코드

    private Integer quantity; // 상품 수량

    private Amount amount; // 결제 금액 정보 -> 클래스 따로 뺌

    @JsonProperty("card_info")
    private CardInfo cardInfo; // 결제 상세 정보 (결제 수단이 카드일 경우)

    @JsonProperty("created_at")
    private String createdAt; // 결제 준비 요청 시각

    @JsonProperty("approved_at")
    private String approvedAt; // 결제 승인 시각

    private String payload; // 결제 승인 요청에 대해 저장한 값
}
