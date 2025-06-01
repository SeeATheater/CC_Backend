package cc.backend.amateurShow.entity;

import cc.backend.common.entity.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AmateurShow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    private String name;

    private String place;

    private String schedule;

    // 추가
    private String runtime;

    @ColumnDefault("0")
    private Integer soldTicket;

    private String account;

    private String hashtag;

    private String contact;

    private String rejectReason;

    private Integer cancelFee;

    private String summary;

    private String posterImageUrl;

//    @Enumerated(EnumType.STRING)
//    @Builder.Default
//    private AmateurStatus amateurStatus = AmateurStatus.YET;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany (mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<PhotoAlbum> photoAlbums = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurTicket> amateurTicketList = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurCasting> amateurCastingList = new ArrayList<>();

    @OneToOne(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private AmateurNotice amateurNotice;

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurStaff> amateurStaffList = new ArrayList<>();

    //--공연 회차랑 날짜 넣기--
    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<AmateurRounds> amateurRounds = new ArrayList<>();

    @Builder
    public AmateurShow(String name, String place, String schedule, List<PhotoAlbum> photoAlbums) {
        this.name = name;
        this.place = place;
        this.photoAlbums = photoAlbums;
    }
}
