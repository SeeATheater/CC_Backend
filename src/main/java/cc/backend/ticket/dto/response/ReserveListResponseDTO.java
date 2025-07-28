package cc.backend.ticket.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReserveListResponseDTO {

    private String showTitle;
    private String showPosterUrl;
    private String place;
    private String period;

    private List<RoundReservationInfo> rounds;

    @Getter
    @Builder
    public static class RoundReservationInfo {
        private Long roundId;
        private Integer roundNumber;
        private String performanceDateTime; // YYYY-MM-DD (요일) HH:mm
        private Integer reservedCount;
        private Integer totalTicket;
        private List<TicketDetail> ticketDetails;
    }

    @Getter
    @Builder
    public static class TicketDetail {
        private String discountName;
        private Integer price;
        private Integer quantity;
    }
}