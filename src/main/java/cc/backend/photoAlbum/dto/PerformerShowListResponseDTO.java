package cc.backend.photoAlbum.dto;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "공연진 공연 모아보기 응답 DTO")
public class PerformerShowListResponseDTO {
    @Schema(description = "총 등록된 공연 수", example = "4")
    private long totalCount;

    @Schema(description = "공연진 이름")
    private String performerName;

    @Schema(description = "공연 카드 목록")
    private List<ShowList> shows;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "공연 카드 한 항목")
    public static class ShowList {
        @Schema(description = "공연 ID")
        private Long showId;
        @Schema(description = "공연명")
        private String title;
        @Schema(description = "포스터 url")
        private String posterImageUrl;
        @Schema(description = "공연장 주소")
        private String detailAddress;
        @Schema(description = "공연 상태")
        private AmateurShowStatus status;

        public static ShowList from(AmateurShow s) {
            return ShowList.builder()
                    .showId(s.getId())
                    .title(s.getName())
                    .posterImageUrl(s.getPosterImageUrl())
                    .detailAddress(s.getDetailAddress())
                    .status(s.getStatus())
                    .build();
        }
    }


}
