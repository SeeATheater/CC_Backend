package cc.backend.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class VisitResponseDTO {

    @Getter
    @AllArgsConstructor
    public static class HourlyVisitorDTO {
        private String hour;
        private Long count;
    }

    @Getter
    @AllArgsConstructor
    public static class MonthlyVisitorDTO {
        private String month;
        private Long count;
    }
}
