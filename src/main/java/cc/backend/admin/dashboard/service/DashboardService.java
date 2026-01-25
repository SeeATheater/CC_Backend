package cc.backend.admin.dashboard.service;

import cc.backend.admin.dashboard.dto.ApprovalSummaryResponseDTO;
import cc.backend.admin.dashboard.dto.ReservationSummaryResponseDTO;
import cc.backend.admin.dashboard.dto.VisitResponseDTO;
import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.PageResponse;
import com.google.analytics.data.v1beta.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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


        Map<Integer, Long> hourMap = IntStream.range(0, 24)
                .boxed()
                .collect(Collectors.toMap(h -> h, h -> 0L));

        response.getRowsList().forEach(row -> {
            int hour = Integer.parseInt(row.getDimensionValues(0).getValue());
            long count = Long.parseLong(row.getMetricValues(0).getValue());
            hourMap.put(hour, count);
        });

        return hourMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new VisitResponseDTO.HourlyVisitorDTO(e.getKey(), e.getValue()))
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

    public PageResponse<ApprovalSummaryResponseDTO> getApprovalList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<AmateurShow> result = amateurShowRepository.findAll(pageable);

        Page<ApprovalSummaryResponseDTO> dtoPage = result.map(this::toSummaryDto);

        return PageResponse.of(dtoPage);
    }

    private ApprovalSummaryResponseDTO toSummaryDto(AmateurShow s) {
        String dateTime = s.getCreatedAt().format(DT_FMT);
        int capacity = s.getTotalSoldTicket() != null ? s.getTotalSoldTicket() : 0; // null이면 0으로 처리


        return ApprovalSummaryResponseDTO.builder()
                .showId(s.getId())
                .showName(s.getName())
                .dateTime(dateTime)
                .capacity(capacity)
                .build();
    }

    public PageResponse<ReservationSummaryResponseDTO> getReservationList(int page, int size) {
        Sort sort = Sort.by(
                Sort.Order.asc("performanceDateTime"),
                Sort.Order.asc("id")
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AmateurRounds> result = amateurRoundsRepository.findAll(pageable);

        Page<ReservationSummaryResponseDTO> dtoPage = result.map(this::toDto);
        return PageResponse.of(dtoPage);
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
