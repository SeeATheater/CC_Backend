package cc.backend.auth.kakao.dto.request;

import cc.backend.member.enumerate.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCallbackRequest {
    @NotBlank
    private String code;

    @NotNull
    private Role role;
}