package cc.backend.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AmateurTicketListDTO {
    private Long amateurTicketId;
    private String discountName;
    private Integer price;
}
