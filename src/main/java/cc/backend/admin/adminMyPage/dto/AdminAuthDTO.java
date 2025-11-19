package cc.backend.admin.adminMyPage.dto;


import lombok.*;

public class AdminAuthDTO {
    @Getter
    @Setter @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        private String email;   // 관리자 아이디
        private String password;   // 평문 비밀번호
    }

    @Getter @Builder @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String tokenType;
        private long   expiresIn;
        private String username;
        private String name;
        private String role;
    }
}
