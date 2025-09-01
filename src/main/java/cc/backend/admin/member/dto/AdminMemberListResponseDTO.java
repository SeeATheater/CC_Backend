package cc.backend.admin.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminMemberListResponseDTO {
    private Long memberId;

    private String username;

    private String name;

    private String email;

    private String phone;

}
