package cc.backend.admin.dashboard.service;

import cc.backend.admin.dashboard.dto.ApprovalSummaryResponseDTO;
import cc.backend.admin.dashboard.dto.ReservationSummaryResponseDTO;
import cc.backend.admin.dashboard.dto.VisitResponseDTO;
import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import com.google.analytics.data.v1beta.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final BetaAnalyticsDataClient betaAnalyticsDataClient;
    private final String propertyId = System.getenv("GA_PROPERTY_ID");

    private final AmateurShowRepository amateurShowRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;

    public List<VisitResponseDTO.HourlyVisitorDTO> getHourlyVisits() {
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate("today")
                        .setEndDate("today"))
                .addDimensions(Dimension.newBuilder().setName("hour"))
                .addMetrics(Metric.newBuilder().setName("activeUsers"))
                .build();

        RunReportResponse response = betaAnalyticsDataClient.runReport(request);

        return response.getRowsList().stream()
                .map(row -> new VisitResponseDTO.HourlyVisitorDTO(
                        row.getDimensionValues(0).getValue(),
                        Long.parseLong(row.getMetricValues(0).getValue())
                ))
                .toList();
    }

    public List<VisitResponseDTO.MonthlyVisitorDTO> getMonthlyVisits() {
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId)
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate("2025-01-01")
                        .setEndDate("today"))
                .addDimensions(Dimension.newBuilder().setName("month"))
                .addMetrics(Metric.newBuilder().setName("activeUsers"))
                .build();

        RunReportResponse response = betaAnalyticsDataClient.runReport(request);

        return response.getRowsList().stream()
                .map(row -> new VisitResponseDTO.MonthlyVisitorDTO(
                        row.getDimensionValues(0).getValue(),
                        Long.parseLong(row.getMetricValues(0).getValue())
                ))
                .toList();
    }


    // 시간 포맷때문에 static 선언했는데, 프론트랑 상의 후 수정 예정
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd / HH:mm");

    public Slice<ApprovalSummaryResponseDTO> getApprovalList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<AmateurShow> result = amateurShowRepository.findAll(pageable);

        List<ApprovalSummaryResponseDTO> content = result.getContent().stream()
                .map(this::toSummaryDto)
                .toList();

        return new SliceImpl<>(content, pageable, result.hasNext());
    }

    private ApprovalSummaryResponseDTO toSummaryDto(AmateurShow s) {
        String dateTime = s.getCreatedAt().format(DT_FMT);
        int capacity    = s.getTotalSoldTicket();

        return ApprovalSummaryResponseDTO.builder()
                .showId(s.getId())
                .showName(s.getName())
                .dateTime(dateTime)
                .capacity(capacity)
                .build();
    }

    public Slice<ReservationSummaryResponseDTO> getReservationList(int page, int size) {
        Sort sort = Sort.by(
                Sort.Order.asc("performanceDateTime"),
                Sort.Order.asc("id")
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AmateurRounds> result = amateurRoundsRepository.findAll(pageable);

        List<ReservationSummaryResponseDTO> content = result.getContent().stream()
                .map(this::toDto)
                .toList();

        return new SliceImpl<>(content, pageable, result.hasNext());
    }

    private ReservationSummaryResponseDTO toDto(AmateurRounds r) {
        return ReservationSummaryResponseDTO.builder()
                .amateurRoundId(r.getId())
                .showName(r.getAmateurShow().getName())
                .performanceDateTime(r.getPerformanceDateTime())
                .totalTicket(r.getTotalTicket())
                .build();
    }
}
