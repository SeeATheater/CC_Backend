package cc.backend.auth.controller;


import cc.backend.config.jwt.CustomUserDetails;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.RefreshTokenRequest;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Tag(name = "Auth", description = "Auth API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    // 허용된 dev 계정 목록
    private static final Set<String> ALLOWED_EMAILS = Set.of(
            "user@test.com",
            "performer@test.com",
            "admin@test.com"
    );

    @Operation(
            summary = "개발자용 토큰 발급",
            description = """
            개발/테스트 환경에서만 사용할 수 있는 토큰 발급 API입니다.
            <br><br>
            <b>허용된 계정만 사용 가능합니다.</b><br>
            - user@test.com<br>
            - performer@test.com<br>
            - admin@test.com
            """
    )
    @PostMapping("/dev/login")
    public ResponseEntity<TokenDTO> devLogin(
            @RequestParam String email
    ) {
        // 화이트리스트 체크
        if (!ALLOWED_EMAILS.contains(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "허용되지 않은 계정입니다.");
        }

        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "계정이 존재하지 않습니다."));

        TokenDTO tokenDto = tokenProvider.generateTokenDto(member);

        return ResponseEntity.ok(tokenDto);
    }

    @Operation(
            summary = "개발자용 액세스 토큰 재발급",
            description = "refresh token을 통해 access token을 재발급합니다."
    )
    @PostMapping("/dev/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody RefreshTokenRequest request) {
        TokenDTO tokenDto = tokenProvider.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(tokenDto);
    }
    @Operation(
            summary = "로그아웃"
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        tokenProvider.logout(userDetails.getMember().getId());
        return ResponseEntity.ok().build();
    }
}