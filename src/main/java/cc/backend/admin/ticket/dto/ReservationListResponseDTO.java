package cc.backend.admin.ticket.dto;

import cc.backend.ticket.entity.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ReservationListResponseDTO {
    private Long realTicketId;
    private String reserverName;
    private String showTitle;
    private LocalDateTime performanceDateTime;
    private String detailAddress;
    private int quantity;
    private String status;
}
