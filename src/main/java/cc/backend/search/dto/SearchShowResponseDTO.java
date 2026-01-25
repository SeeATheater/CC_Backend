package cc.backend.search.dto;

import cc.backend.amateurShow.converter.AmateurConverter;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class SearchShowResponseDTO {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "검색 결과 - 소극장 공연 상세 응답 DTO")
    public static class SearchShowDTO{
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

        @Schema(description = "포스터 URL")
        private String posterImageUrl;

        @Schema(description = "공연 상태", example = "판매중")
        private String status;

        public static SearchShowDTO from(AmateurShow s){
            String statusLabel =
                    switch (s.getStatus()) {
                        case ONGOING -> "판매 중";   // 지금 날짜가 공연 날짜 안에 있을 때 (예매 진행 중)
                        case ENDED   -> "공연 종료";  // 지금 날짜가 공연 날짜보다 지났을 때 (공연 종료됨)
                        case YET     -> "공연 예정";   // 지금 날짜가 공연 날짜 안에 없을 때 (아직 공연 예정)
                        case REJECT -> "X";
            };
//            String statusLabel = s.getStatus().toString();
            String schedule = AmateurConverter.mergeSchedule(s.getStart(), s.getEnd());
            return SearchShowDTO.builder()
                    .showId(s.getId())
                    .title(s.getName())
                    .performerName(s.getPerformerName())
                    .hallName(s.getHallName())
                    .schedule(schedule)
                    .status(statusLabel)
                    .posterImageUrl(s.getPosterImageUrl())
                    .build();
        }

        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Schema(description = "검색 결과")
        public static class SearchShowResultDTO{
            private List<SearchShowDTO> searchShowDTOs;
            private long total;
        }


    }





}
