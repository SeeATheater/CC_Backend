package cc.backend.member;

import cc.backend.member.enumerate.Role;
import cc.backend.member.google.AuthResponse;
import cc.backend.member.google.GoogleOauthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {

    private final GoogleOauthService googleOauthService;

    @GetMapping("/login/oauth2/code/google") //http://localhost:8080/login/oauth2/code/google
    @Operation(summary = "구글 로그인")
    public AuthResponse loginWithGoogle(@RequestParam("code") String code, @RequestParam("state") String state) {
        Role role = switch (state){
            case "audience" -> Role.AUDIENCE;
            case "performer" -> Role.PERFORMER;
            default -> throw new IllegalStateException("Unexpected value: " + state);
        };
        return googleOauthService.authenticateWithGoogle(code, role);
    }


}
