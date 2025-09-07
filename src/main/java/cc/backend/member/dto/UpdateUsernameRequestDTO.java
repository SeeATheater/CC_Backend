package cc.backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUsernameRequestDTO {
    @NotBlank(message = "유저네임은 필수입니다.")
    @Size(min = 1, max = 20, message = "유저네임은 1-20자 사이여야 합니다.")
    private String username;
}
