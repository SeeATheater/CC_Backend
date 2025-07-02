package cc.backend.kakaoPay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo {

    @JsonProperty("kakaopay_purchase_corp")
    private String kakaopayPurchaseCorp; // 카카오페이 매입사명

    @JsonProperty("kakaopay_purchase_corp_code")
    private String kakaopayPurchaseCorpCode; // 카카오페이 매입사 코드

    @JsonProperty("kakaopay_issuer_corp")
    private String kakaopayIssuerCorp; // 카카오페이 발급사명

    @JsonProperty("kakaopay_issuer_corp_code")
    private String kakaopayIssuerCorpCode; // 카카오페이 발급사 코드

    private String bin; // 카드 BIN

    @JsonProperty("card_type")
    private String cardType; // 카드 타입

    @JsonProperty("install_month")
    private String installMonth; // 할부 개월 수

    @JsonProperty("approved_id")
    private String approvedId; // 카드사 승인번호

    @JsonProperty("card_mid")
    private String cardMid; // 카드사 가맹점 번호

    @JsonProperty("interest_free_install")
    private String interestFreeInstall; // 무이자할부 여부

    @JsonProperty("installment_type")
    private String installmentType; // 할부 유형

    @JsonProperty("card_item_code")
    private String cardItemCode; // 카드 상품 코드

}
