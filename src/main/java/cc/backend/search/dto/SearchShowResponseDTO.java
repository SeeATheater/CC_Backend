package cc.backend.search.dto;

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
public class SearchShowResponseDTO {
    @Schema(description = "공연 ID", example = "1")
    private Long showId;

    @Schema(description = "공연명", example = "실종")
    private String title;

    @Schema(description = "공연진명", example = "홍길동")
    private String performerName;

    @Schema(description = "공연장명", example = "문학관 대학로 3층 소극장")
    private String hallName;

    @Schema(description = "일정 문자열", example = "2024.04.03(목) 19:00 ~ 2024.10.05(토) 14:00")
    private String schedule;

    @Schema(description = "포스터 이미지 URL")
    private String posterImageUrl;
    @Schema(description = "상태 라벨", example = "판매중")
    private AmateurShowStatus status;

    public static SearchShowResponseDTO from(AmateurShow s){
        return SearchShowResponseDTO.builder()
                .showId(s.getId())
                .title(s.getName())
                .performerName(s.getPerformerName())
                .hallName(s.getHallName())
                .schedule(s.getSchedule())
                .posterImageUrl(s.getPosterImageUrl())
                .status(s.getStatus())
                .build();
    }


}
