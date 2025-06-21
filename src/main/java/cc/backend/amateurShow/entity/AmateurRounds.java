package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AmateurRounds {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer roundNumber;

    private LocalDateTime performanceDateTime;

    private Integer totalTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amateur_show_id")
    private AmateurShow amateurShow;

    public void update(AmateurUpdateRequestDTO.UpdateRounds dto) {
        if (dto.getRoundNumber() != null) this.roundNumber = dto.getRoundNumber();
        if (dto.getPerformanceDateTime() != null) this.performanceDateTime = dto.getPerformanceDateTime();
        if (dto.getTotalTicket() != null) this.totalTicket = dto.getTotalTicket();
    }
}