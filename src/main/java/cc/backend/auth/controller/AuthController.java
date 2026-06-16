package cc.backend.auth.controller;


import cc.backend.config.jwt.CustomUserDetails;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Auth API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenProvider tokenProvider;

    @Operation(
            summary = "로그아웃"
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            tokenProvider.logout(userDetails.getMember().getId());
        } else if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            tokenProvider.logoutByRefreshToken(request.getRefreshToken());
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(tokenProvider.refreshAccessToken(request.getRefreshToken()));
    }
}
