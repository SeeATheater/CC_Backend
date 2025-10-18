package cc.backend.admin.account;

import cc.backend.admin.account.dto.AccountRequest;
import cc.backend.admin.account.dto.AccountResponse;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/account")
@Tag(name  = "관리자 계좌 관리(최소 호출)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @PostMapping
    @Operation(summary = "계좌 생성", description = "새로운 관리자 계좌를 생성합니다.")
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response = adminAccountService.createAccount(member.getId(),request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "계좌 수정", description = "관리자 계좌를 수정합니다.")
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "계좌 ID", required = true) @PathVariable Long accountId,
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response = adminAccountService.updateAccount(member.getId(),accountId,request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "관리자 계좌 조회", description = "관리자 계좌를 조회합니다.")
    public ResponseEntity<List<AccountResponse>> getAccounts() {

        List<AccountResponse> responses = adminAccountService.getAccounts();
        return ResponseEntity.ok(responses);
    }

}
