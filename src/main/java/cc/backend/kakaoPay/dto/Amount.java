package cc.backend.kakaoPay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Amount {
    private Integer total; // 전체 결제 금액

    @JsonProperty("tax_free")
    private Integer taxFree; // 비과세 금액

    private Integer vat; // 부가세 금액

    private Integer point; // 사용할 포인트 금액

    private Integer discount; // 할인 금액

    @JsonProperty("green_deposit")
    private Integer greenDeposit; // 컵 보증금
}
