package cc.backend.admin.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TicketDetailResponseDTO {
    private Long realTicketId;
    private String showTitle;
    private LocalDateTime performanceDateTime;
    private int quantity;

}
