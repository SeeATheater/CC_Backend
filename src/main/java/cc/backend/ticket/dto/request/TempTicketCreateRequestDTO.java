package cc.backend.ticket.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TempTicketCreateRequestDTO {
    @Positive(message = "TEMP_TICKET_QUANTITY")
    private int quantity;               // 수량
}
