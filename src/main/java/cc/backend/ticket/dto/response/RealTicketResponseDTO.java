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

        private LocalDateTime cancelAvailableUntil; // 취소 가능 기한
        private String cancelFeePolicyText;      // 취소 수수료 정책 안내
        private Integer cancelFee;               // 실제 계산된 취소 수수료
        private Integer cancelAmount;            // 최종 환불(취소) 금액

        public static RealTicketResponseDTO from(RealTicket ticket){

            return RealTicketResponseDTO.builder()
                    .ticketId(ticket.getId())
                    .showTitle(ticket.getShowTitle())
                    .detailAddress(ticket.getDetailAddress())
                    .performanceDateTime(ticket.getPerformanceDateTime())
                    .reserveDateTime(ticket.getReserveDateTime())
                    .quantity(ticket.getQuantity())
                    .totalPrice(ticket.getTotalPrice())
                    .reservationStatus(ticket.getReservationStatus())
                    .posterImageUrl(ticket.getPosterImageUrl())
                    //.cancelAvailableUntil(ticket.getCancelAvailableUntil())
                    //.cancelFeePolicyText(ticket.getCancelFeePolicyText())
                    .build();
        }

        // 취소 완료 응답용 (수수료, 환불액 포함)
        public static RealTicketResponseDTO from(RealTicket ticket, int cancelFee, int cancelAmount) {
                return RealTicketResponseDTO.builder()
                                            .ticketId(ticket.getId())
                                            .showTitle(ticket.getShowTitle())
                                            .detailAddress(ticket.getDetailAddress())
                                            .performanceDateTime(ticket.getPerformanceDateTime())
                                            .reserveDateTime(ticket.getReserveDateTime())
                                            .quantity(ticket.getQuantity())
                                            .totalPrice(ticket.getTotalPrice())
                                            .reservationStatus(ticket.getReservationStatus()) // 이 시점에는 CANCELLED 상태
                                            .cancelAvailableUntil(ticket.getCancelAvailableUntil())
                                            .posterImageUrl(ticket.getPosterImageUrl())
                                            .cancelFeePolicyText(ticket.getCancelFeePolicyText())
                                            .cancelFee(cancelFee) // 계산된 수수료
                                            .cancelAmount(cancelAmount) // 최종 환불액
                                            .build();
        }
}
