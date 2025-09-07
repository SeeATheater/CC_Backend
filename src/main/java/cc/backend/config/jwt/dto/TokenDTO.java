package cc.backend.config.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TokenDTO {
    private String accessToken;
    private String refreshToken;
}
