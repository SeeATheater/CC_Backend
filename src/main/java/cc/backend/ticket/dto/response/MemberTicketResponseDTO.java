package cc.backend.ticket.dto.response;


import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberTicketResponseDTO {

    //private String posterUrl;
    private Long ticketId;
    private String showTitle;
    private int quantity;
    private String place;
    private LocalDateTime reserveDate;
    private LocalDateTime performanceDateTime;
    private LocalDateTime cancelAvailableUntil;
    private ReservationStatus reservationStatus;

    public static MemberTicketResponseDTO from(MemberTicket ticket) {
        return MemberTicketResponseDTO.builder()
                //.posterUrl(ticket.getAmateurTicket().getAmateurShow().getPosterUrl())
                .ticketId(ticket.getId())
                .showTitle(ticket.getAmateurTicket().getAmateurShow().getName())
                .place(ticket.getAmateurTicket().getAmateurShow().getPlace())
                .quantity(ticket.getQuantity())
                .reserveDate(ticket.getReserveDate())
                .performanceDateTime(ticket.getPerformanceDateTime())
                .cancelAvailableUntil(ticket.getCancelAvailableUntil())
                .reservationStatus(ticket.getReservationStatus())
                .build();
    }
}
