package cc.backend.ticket.entity;

import cc.backend.amateurShow.entity.AmateurRounds;
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
public class TempTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private LocalDateTime reserveDate;

    @Column(nullable = false)
    private LocalDateTime performanceDateTime;

    private LocalDateTime cancelAvailableUntil;

    @Column(nullable = false)
    private int totalPrice;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amateur_ticket_id")
    private AmateurTicket amateurTicket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "amateur_round_id")
    private AmateurRounds amateurRound;

    public void updateReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    // --카카오 결제--
    @Column(name = "kakao_tid")
    private String kakaoTid; // 결제 승인 후 저장용

    public void updateTid(String tid) {
        this.kakaoTid = tid;
    }


}
