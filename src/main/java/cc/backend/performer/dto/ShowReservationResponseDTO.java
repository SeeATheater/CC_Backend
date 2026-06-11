package cc.backend.performer.dto;

import cc.backend.ticket.entity.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "특정 공연의 예매 내역 응답 DTO")
public class ShowReservationResponseDTO {

    @Schema(description = "공연 ID")
    private Long showId;
    @Schema(description = "공연명")
    private String showTitle;
    @Schema(description = "포스터 이미지 url")
    private String posterImageUrl;
    @Schema(description = "공연장 도로명 주소")
    private String roadAddress;
    @Schema(description = "공연장 상세 주소")
    private String detailAddress;

    @Schema(description = "공연 기간")
    private String schedule;

    @Schema(description = "회차 요약(표 상단)")
    private List<RoundSummary> roundSummaries;

    @Schema(description = "선택된 회차 ID")
    private Long selectedRoundId;

    @Schema(description = "선택된 회차 관람일시")
    private LocalDateTime selectedPerformanceDateTime;

    @Schema(description = "선택된 회차의 상세 예매 내역")
    private List<ReservationRow> reservations;



    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "회차 요약 정보")
    public static class RoundSummary {
        @Schema(description = "회차 ID")        private Long roundId;
        @Schema(description = "회차 번호")       private Integer roundNumber;
        @Schema(description = "관람일시")        private LocalDateTime performanceDateTime;
        @Schema(description = "해당 회차 누적 인원") private int sumQuantity;
        @Schema(description = "해당 회차 누적 금액") private int sumAmount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "선택 회차의 예매 상세 행")
    public static class ReservationRow {
        @Schema(description = "예매자명")  private String reserverName;
        @Schema(description = "인원수")    private int quantity;
        @Schema(description = "결제 상태") private ReservationStatus reservationStatus;

    }
}
