package cc.backend.ticket.entity;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.ticket.entity.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTicket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예매한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amateur_rounds_id", nullable = false)
    private AmateurRounds amateurRound;

    // 공연 제목
    private String showTitle;

    // 포스터 이미지 URL
    private String posterImageUrl;

    // 장소 (ex. 홍익대학교 학생회관 3층 소극장)
    //private String place;

    private String detailAddress; // 상세 주소 -> 이게 홍익대학교 학생회관 3층 소극장

    // 관람일시
    private LocalDateTime performanceDateTime;

    // 예매일자
    private LocalDateTime reserveDateTime;

    // 예매 수량
    private int quantity;

    // 예매 금액
    private int totalPrice;

    // 예매 상태 (ex. 예매 완료, 예매 취소 등)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReservationStatus reservationStatus = ReservationStatus.PENDING;

    // 취소 가능 일시
    private LocalDateTime cancelAvailableUntil;

    // 취소 수수료 정책 (옵션)
    private String cancelFeePolicyText;

    @Column(name = "kakao_tid")
    private String kakaoTid; // 결제 승인 후 저장용

    public void updateReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

}
