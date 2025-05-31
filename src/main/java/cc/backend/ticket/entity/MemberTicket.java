package cc.backend.ticket.entity;

import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.ticket.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private LocalDateTime reserveDate;

    private LocalDateTime performanceDateTime;

    private LocalDateTime cancelAvailableUntil;

    private int totalPrice;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amateur_ticket_id")
    private AmateurTicket amateurTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public void cancelTicket(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public void updateMemberTicket(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }
}
