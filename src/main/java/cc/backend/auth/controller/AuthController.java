package cc.backend.auth.controller;


import cc.backend.config.jwt.CustomUserDetails;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.config.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<Void> logout(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        tokenProvider.logout(userDetails.getMember().getId());
        return ResponseEntity.ok().build();
    }
}
