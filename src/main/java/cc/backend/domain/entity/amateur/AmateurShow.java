package cc.backend.domain.entity.amateur;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import cc.backend.domain.common.BaseEntity;
import cc.backend.domain.entity.member.Member;
import cc.backend.domain.enums.AmateurStatus;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmateurShow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String troupe; // 공연진 이름

    private String posterImgUrl;

    private String place;

    private String schedule;

    private String runtime;

    private String age;

    private String starring; // 출연자 목록, AmateurCasting 과는 다른기능입니다

    private Integer totalTicket;

    @ColumnDefault("0")
    private Integer soldTicket;

    private String timeInfo; // 공연시간 정보, runtime과 다름

    //private String staff; // 제거 예정,

    private String account;

    private String hashtag;

    private String contact;

    private String rejectReason;

    private Integer cancelFee;

    private Integer performanceRounds; // 공연 회차

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AmateurStatus amateurStatus = AmateurStatus.YET;


    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurTicket> amateurTicketList = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurCasting> amateurCastingList = new ArrayList<>();

    @OneToOne(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private AmateurNotice amateurNotice;

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurStaff> amateurStaffList = new ArrayList<>();

    @OneToOne(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private AmateurSummary amateurSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}