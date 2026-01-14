package cc.backend.ticket.dto.response;

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
public class TempTicketCreateResponseDTO {
    private Long tempTicketId;
    private String showTitle;               // 공연 이름
    private String detailAddress;           // 상세 주소
    private int quantity;                   // 예매 수량
    private LocalDateTime reserveDate;      // 예매일
    private LocalDateTime performanceDateTime; // 관람일시
    private LocalDateTime cancelAvailableUntil; // 취소 가능 기한
    private int totalPrice;                 // 결제 금액
    private String discountName;            // 할인명
    private ReservationStatus reservationStatus; // 상태 (예매 완료 / 취소 등)


}
