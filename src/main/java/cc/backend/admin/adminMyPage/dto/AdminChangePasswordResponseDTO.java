package cc.backend.admin.adminMyPage.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangePasswordResponseDTO {

    private Long memberId;
    private LocalDateTime changedAt;
}

