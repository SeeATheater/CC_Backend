package cc.backend.admin.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMemberDetailRequestDTO {
    @Schema(description = "아이디(로그인용)", example = "jihee")
    @NotBlank
    private String username;

    @Schema(description = "이름", example = "하지희")
    @NotBlank
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @NotBlank
    private String phone;

    @Schema(description = "이메일", example = "jihee@gmail.com")
    @NotBlank @Email
    private String email;

    @Schema(description = "생년월일(YYYY-MM-DD)", example = "2003-04-27")
    @NotBlank
    private String birth_date;

    @Schema(description = "성별", example = "여")
    @NotBlank
    private String gender;

    @Schema(description = "주소", example = "서울특별시")
    @NotBlank
    private String address;
}
