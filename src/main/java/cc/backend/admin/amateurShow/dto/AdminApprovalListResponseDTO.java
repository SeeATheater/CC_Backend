package cc.backend.admin.amateurShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminApprovalListResponseDTO {
    private Long showId;
    private String username;
    private String memberName;
    private String email;
    private String phone;
    private String showName;
    private String amateurShowStatus;
}
