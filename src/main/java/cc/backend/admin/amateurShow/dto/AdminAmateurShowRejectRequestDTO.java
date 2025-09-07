package cc.backend.admin.amateurShow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAmateurShowRejectRequestDTO {
    @Schema(description = "반려 사유", example = "계좌 정보가 확인되지 않음")
    private String rejectReason;
}
