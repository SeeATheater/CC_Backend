package cc.backend.performer.dto;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "검색 결과 - 소극장 공연 상세 응답 DTO")
public class PerformerMyShowResponseDTO {
    @Schema(description = "공연 ID")
    private Long showId;

    @Schema(description = "공연명")
    private String title;

    @Schema(description = "공연장명")
    private String hallName;

    @Schema(description = "관람 일시")
    private String schedule;

    @Schema(description = "공연 상태")
    private String status;

    public static PerformerMyShowResponseDTO from(AmateurShow s) {
        String statusLabel = switch (s.getStatus()) {
            case APPROVED_ONGOING -> "예매 진행 중";
            case APPROVED_ENDED   -> "공연 종료";
            case APPROVED_YET     -> "예정";
            case WAITING_APPROVAL -> "승인 대기";
            case REJECTED         -> "반려";
        };

        return PerformerMyShowResponseDTO.builder()
                .showId(s.getId())
                .title(s.getName())
                .hallName(s.getHallName())
                .schedule(s.getSchedule())
                .status(statusLabel)
                .build();
    }
}
