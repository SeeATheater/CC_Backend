package cc.backend.member.dto;
import cc.backend.member.enumerate.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginAccessTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long id;
    private String username;
    private String name;
    private Role role;
}

