package cc.backend.member.dto;

import cc.backend.member.enumerate.ActiveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MyPageResponseDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String address;
    private ActiveStatus status;
}
