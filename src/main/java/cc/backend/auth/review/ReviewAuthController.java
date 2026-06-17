package cc.backend.auth.review;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * [임시] 앱 심사용 일반 로그인 컨트롤러.
 *
 * 운영 환경에서도 동작하며, {@link ReviewAccounts#WHITELIST} 에 등록된 계정만
 * 이메일 + 비밀번호로 로그인할 수 있다. 심사 통과 후 revert 대상.
 */
@Tag(name = "Auth", description = "Auth API")
@RestController
@RequestMapping("/auth/review")
@RequiredArgsConstructor
public class ReviewAuthController {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Operation(
            summary = "[임시] 앱 심사용 일반 로그인",
            description = "심사용 화이트리스트 계정에 한해 이메일/비밀번호로 토큰을 발급합니다. 심사 종료 후 제거 예정."
    )
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody LoginRequest req) {
        // 화이트리스트에 없는 계정은 거부 (운영 일반 사용자 차단)
        if (!ReviewAccounts.WHITELIST.containsKey(req.getEmail())) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        Member member = memberRepository.findMemberByEmail(req.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_NOT_MATCH);
        }

        return ResponseEntity.ok(tokenProvider.generateTokenDto(member));
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }
}
