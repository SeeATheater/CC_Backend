package cc.backend.performer.dto;

import cc.backend.amateurShow.entity.AmateurShowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PerformerEnrolledShowResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyEnrolledAmateurShowDetail { // 등록한 공연 조회
        @Schema(description = "공연 ID")
        private Long amateurShowId;
        @Schema(description = "공연 이름")
        private String amateurShowName;
        // 나중에 예매 시작일이 들어가야함
        @Schema(description = "공연 장소")
        private String detailAddress;
        @Schema(description = "관람 일시")
        private String schedule; // 피그마 상 관람 일시
        @Schema(description = "공연 상태")
        private AmateurShowStatus status; // 공연 상태
        @Schema(description = "반려 사유", nullable = true)
        private String rejectReason; // 반려 인 공연들의 반려 사유
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyEnrolledAmateurShowList {

        private List<MyEnrolledAmateurShowDetail> shows;
        private int page;
        private int size;
        private boolean hasNext;
    }
}
