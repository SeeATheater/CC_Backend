package cc.backend.ticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MemberTicketCreateRequestDTO {
    private Long memberId;              // 예매자
    private int quantity;               // 수량
}
