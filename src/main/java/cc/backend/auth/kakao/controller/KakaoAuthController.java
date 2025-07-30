package cc.backend.auth.kakao.controller;

import cc.backend.auth.kakao.dto.request.KakaoCallbackRequest;
import cc.backend.auth.service.AuthService;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.enumerate.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthController {

    private final AuthService authService;

    @PostMapping("/callback")
    public ResponseEntity<TokenDTO> kakaoCallback(
            @Valid @RequestBody KakaoCallbackRequest request) {

        TokenDTO tokenDto = authService.kakaoLogin(request.getCode(), request.getRole());

        return ResponseEntity.ok(tokenDto);
    }
}