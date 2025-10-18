package cc.backend.admin.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String bankName;
    private String accountNumber;
    private String accountOwner;
    private Long memberId;

}
