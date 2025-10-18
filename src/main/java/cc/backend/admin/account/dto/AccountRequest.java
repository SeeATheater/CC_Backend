package cc.backend.admin.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountRequest {
    @NotBlank(message = "은행명은 필수입니다.")
    private String bankName;

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNumber;

    @NotBlank(message = "예금주는 필수입니다.")
    private String accountOwner;

}
