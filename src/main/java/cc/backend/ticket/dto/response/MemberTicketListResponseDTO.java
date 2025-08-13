package cc.backend.ticket.dto.response;

import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberTicketListResponseDTO {

    private Long memberTicketId;
    private String bookingNumber;
    private String showTitle;
    private int quantity;
    //private String place;
    private String detailAddress; // 상세 주소
    private LocalDateTime reserveDate;
    private LocalDateTime performanceDateTime;
    private LocalDateTime cancelAvailableUntil;
    private ReservationStatus reservationStatus;

    public static MemberTicketListResponseDTO from(MemberTicket ticket) {
        return MemberTicketListResponseDTO.builder()
                .memberTicketId(ticket.getId())
                .bookingNumber(ticket.getBookingNumber())
                .showTitle(ticket.getAmateurTicket().getAmateurShow().getName())
                //.place(ticket.getAmateurTicket().getAmateurShow().getPlace())
                .detailAddress(ticket.getAmateurTicket().getAmateurShow().getDetailAddress())
                .quantity(ticket.getQuantity())
                .reserveDate(ticket.getReserveDate())
                .performanceDateTime(ticket.getPerformanceDateTime())
                .cancelAvailableUntil(ticket.getCancelAvailableUntil())
                .reservationStatus(ticket.getReservationStatus())
                .build();
    }
}