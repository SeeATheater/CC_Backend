package cc.backend.admin.account;

import cc.backend.admin.account.dto.AccountRequest;
import cc.backend.admin.account.dto.AccountResponse;
import cc.backend.member.entity.Account;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.AccountRepository;
import cc.backend.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AdminAccountService adminAccountService;

    @Test
    void createAccount_throwsWhenMemberIsNotAdmin() {
        Long memberId = 1L;
        Member audience = Member.builder()
                .email("audience@test.com")
                .role(Role.AUDIENCE)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(audience));

        AccountRequest request = AccountRequest.builder()
                .bankName("bank")
                .accountNumber("123-456")
                .accountOwner("owner")
                .build();

        assertThrows(IllegalStateException.class, () -> adminAccountService.createAccount(memberId, request));
    }

    @Test
    void createAccount_throwsWhenMemberRoleIsNull() {
        Long memberId = 1L;
        Member memberWithNullRole = Member.builder()
                .email("null-role@test.com")
                .role(null)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberWithNullRole));

        AccountRequest request = AccountRequest.builder()
                .bankName("bank")
                .accountNumber("123-456")
                .accountOwner("owner")
                .build();

        assertThrows(IllegalStateException.class, () -> adminAccountService.createAccount(memberId, request));
    }

    @Test
    void createAccount_succeedsWhenMemberIsAdmin() {
        Long memberId = 1L;
        Member admin = Member.builder()
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(admin));
        when(memberRepository.existsById(memberId)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenReturn(
                Account.builder()
                        .id(10L)
                        .bankName("bank")
                        .accountNumber("123-456")
                        .accountOwner("owner")
                        .memberId(memberId)
                        .build()
        );

        AccountRequest request = AccountRequest.builder()
                .bankName("bank")
                .accountNumber("123-456")
                .accountOwner("owner")
                .build();

        AccountResponse response = adminAccountService.createAccount(memberId, request);

        assertEquals(10L, response.getId());
        assertEquals(memberId, response.getMemberId());
        assertEquals("bank", response.getBankName());
        assertEquals("123-456", response.getAccountNumber());
        assertEquals("owner", response.getAccountOwner());
    }
}
