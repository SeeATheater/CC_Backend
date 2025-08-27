package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.ticket.entity.RealTicket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "amateurRound", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<RealTicket> realTickets = new ArrayList<>();

    public void update(AmateurUpdateRequestDTO.UpdateRounds dto) {
        if (dto.getRoundNumber() != null) this.roundNumber = dto.getRoundNumber();
        if (dto.getPerformanceDateTime() != null) this.performanceDateTime = dto.getPerformanceDateTime();
        if (dto.getTotalTicket() != null) this.totalTicket = dto.getTotalTicket();
    }

    public void decreaseTotalTicket(int quantity) {
        if (this.totalTicket == null) {
            this.totalTicket = 0;
        }
        this.totalTicket -= quantity;
    }

}