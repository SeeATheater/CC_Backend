package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.common.entity.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany (mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoAlbum> photoAlbums = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<AmateurTicket> amateurTicketList = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<AmateurCasting> amateurCastingList = new ArrayList<>();

    @OneToOne(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    private AmateurNotice amateurNotice;

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<AmateurStaff> amateurStaffList = new ArrayList<>();

    //--공연 회차랑 날짜 넣기--
    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<AmateurRounds> amateurRounds = new ArrayList<>();

    @Builder
    public AmateurShow(String name, String place, String schedule, List<PhotoAlbum> photoAlbums) {
        this.name = name;
        this.place = place;
        this.photoAlbums = photoAlbums;
    }

    public void updateInfo(AmateurUpdateRequestDTO dto) {
        if (dto.getName() != null) this.name = dto.getName();
        if (dto.getPlace() != null) this.place = dto.getPlace();
        if (dto.getSchedule() != null) this.schedule = dto.getSchedule();
        if (dto.getRuntime() != null) this.runtime = dto.getRuntime();
        if (dto.getAccount() != null) this.account = dto.getAccount();
        if (dto.getContact() != null) this.contact = dto.getContact();
        if (dto.getHashtag() != null) this.hashtag = dto.getHashtag();
        if (dto.getSummary() != null) this.summary = dto.getSummary();
        if (dto.getPosterImageUrl() != null) this.posterImageUrl = dto.getPosterImageUrl();
    }

}
