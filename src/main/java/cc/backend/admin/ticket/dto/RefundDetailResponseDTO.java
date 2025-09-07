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
public class RefundDetailResponseDTO {
    private Long realTicketId;
    private String showTitle;
    private String memberName;
    private LocalDateTime performanceDateTime;
    private LocalDateTime canceledAt;
    private int totalPrice;
    private int cancelFee;
    private String account;
    private String status;


}
