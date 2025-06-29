package cc.backend.kakaoPay.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Amount {
    private Integer total; // 전체 결제 금액
    private Integer tax_free; // 비과세 금액
    private Integer vat; // 부가세 금액
    private Integer point; // 사용할 포인트 금액
    private Integer discount; // 할인 금액
}
