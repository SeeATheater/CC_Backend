package cc.backend.ticket.dto.response;

import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class RealTicketResponseDTO {

        private Long ticketId;                   // realTicket ID
        private String showTitle;                // 공연 제목
        private String posterImageUrl;           // 포스터 이미지 URL
        //private String place;                    // 장소
        private String detailAddress;            // 상세 주소
        private LocalDateTime performanceDateTime;       // 관람일 (시간 제외)
        private int quantity;                    // 수량
        private int totalPrice;                  // 금액
        private LocalDateTime reserveDateTime;           // 예매일
        private ReservationStatus reservationStatus; // 상태

        public static RealTicketResponseDTO from(RealTicket ticket){
            return RealTicketResponseDTO.builder()
                    .ticketId(ticket.getId())
                    .showTitle(ticket.getShowTitle())
                    .posterImageUrl(ticket.getPosterImageUrl())
                    .detailAddress(ticket.getDetailAddress())
                    .performanceDateTime(ticket.getPerformanceDateTime())
                    .reserveDateTime(ticket.getReserveDateTime())
                    .quantity(ticket.getQuantity())
                    .totalPrice(ticket.getTotalPrice())
                    .reservationStatus(ticket.getReservationStatus())
                    //.cancelAvailableUntil(ticket.getCancelAvailableUntil())
                    //.cancelFeePolicyText(ticket.getCancelFeePolicyText())
                    .build();
        }
}
