package cc.backend.admin.dashboard.service;

import cc.backend.admin.dashboard.dto.VisitResponseDTO;
import com.google.analytics.data.v1beta.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final BetaAnalyticsDataClient betaAnalyticsDataClient;
    private final String propertyId = System.getenv("GA_PROPERTY_ID");

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
}
