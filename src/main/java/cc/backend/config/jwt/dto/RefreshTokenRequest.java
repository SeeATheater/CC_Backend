package cc.backend.config.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "_BAD_REQUEST")
    private String refreshToken;
}
