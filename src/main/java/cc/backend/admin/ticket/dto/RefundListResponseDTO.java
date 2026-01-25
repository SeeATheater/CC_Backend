package cc.backend.admin.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class RefundListResponseDTO {
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class RefundListDTO {
        private Long realTicketId;
        private String username;
        private String memberName;
        private String showTitle;
        private LocalDateTime performanceDateTime;
        private LocalDateTime canceledAt;
    }

}
