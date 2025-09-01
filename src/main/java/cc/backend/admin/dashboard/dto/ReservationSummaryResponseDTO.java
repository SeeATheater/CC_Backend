package cc.backend.admin.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationSummaryResponseDTO {
    private Long amateurRoundId;
    private String showName;
    private LocalDateTime performanceDateTime;
    private int totalTicket;
}
