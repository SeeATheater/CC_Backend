package cc.backend.admin.account;

import cc.backend.admin.account.dto.AccountRequest;
import cc.backend.admin.account.dto.AccountResponse;
import cc.backend.member.entity.Account;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.AccountRepository;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    // 계좌 생성
    @Transactional
    public AccountResponse createAccount(Long memberId, AccountRequest request) {
        // memberId의 role이 admin인지 확인
        validateAdminRole(memberId);

        // memberId 존재 여부 확인
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        Account account = Account.builder()
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountOwner(request.getAccountOwner())
                .memberId(memberId)
                .build();

        Account savedAccount = accountRepository.save(account);
        return toResponse(savedAccount);
    }

    // 계좌 수정
    @Transactional
    public AccountResponse updateAccount(Long memberId, Long accountId, AccountRequest request) {
        validateAdminRole(memberId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계좌입니다."));

        account.update(request.getBankName(), request.getAccountNumber(), request.getAccountOwner());

        return toResponse(account);
    }

    // 계좌 삭제
    @Transactional
    public void deleteAccount(Long memberId, Long accountId) {
        validateAdminRole(memberId);

        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("존재하지 않는 계좌입니다.");
        }

        accountRepository.deleteById(accountId);
    }


    // 계좌 조회
    public List<AccountResponse> getAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateAdminRole(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!member.getRole().equals("ADMIN")) {
            throw new IllegalStateException("관리자 권한이 필요합니다.");
        }
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .accountOwner(account.getAccountOwner())
                .memberId(account.getMemberId())
                .build();
    }

}
