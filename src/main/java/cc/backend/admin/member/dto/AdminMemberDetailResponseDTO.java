package cc.backend.admin.member.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminMemberDetailResponseDTO {
    private Long memberId;
    private String username;
    private String name;
    private String phone;
    private String email;

    // 필요 없어 보이지만 member필드에 있음
    private String birth_date;
    private String gender;
    private String address;


}
