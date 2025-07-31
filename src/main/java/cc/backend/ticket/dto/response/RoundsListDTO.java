package cc.backend.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoundsListDTO {
    private Long roundId;
    private Integer roundNumber;
    private LocalDateTime performanceDateTime;
}
