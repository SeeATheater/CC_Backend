package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

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

    private String schedule;

    private String performerName; // 공연진 이름

    private String hallName; // 공연장 이름

    //private String place; // 공연장 주소

    private String roadAddress; // 공연장 도로명 주소
    private String detailAddress; // 공연장 상세 주소

    private String bankName; // 은행명
    private String account; // 계좌번호
    private String depositor; // 입금자명

    // 추가
    private String runtime;

    private String hashtag;

    private String contact;

    private String rejectReason;

    private Integer cancelFee;

    private String summary;

    private String posterImageUrl;

    private Integer totalSoldTicket;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 30)
    private AmateurShowStatus status = AmateurShowStatus.APPROVED_YET;
//    @Enumerated(EnumType.STRING)
//    @Builder.Default
//    private AmateurStatus amateurStatus = AmateurStatus.YET;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany (mappedBy = "amateurShow", cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<PhotoAlbum> photoAlbums = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<AmateurTicket> amateurTicketList = new ArrayList<>();

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<AmateurCasting> amateurCastingList = new ArrayList<>();

    @OneToOne(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private AmateurNotice amateurNotice;

    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<AmateurStaff> amateurStaffList = new ArrayList<>();

    //--공연 회차랑 날짜 넣기--
    @OneToMany(mappedBy = "amateurShow", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<AmateurRounds> amateurRounds = new ArrayList<>();

    @Builder
    public AmateurShow(String name, String detailAddress, String schedule, List<PhotoAlbum> photoAlbums) {
        this.name = name;
        this.detailAddress = detailAddress;
        this.schedule = schedule;
        this.photoAlbums = photoAlbums;
    }

    public void updateInfo(AmateurUpdateRequestDTO dto) {
        if (dto.getName() != null) this.name = dto.getName();
        //if (dto.getPlace() != null) this.place = dto.getPlace();
        if (dto.getPerformerName() != null) this.performerName = dto.getPerformerName();
        if (dto.getHallName() != null) this.hallName = dto.getHallName();
        if (dto.getRoadAddress() != null) this.roadAddress = dto.getRoadAddress();
        if (dto.getDetailAddress() != null) this.detailAddress = dto.getDetailAddress();
        if (dto.getBankName() != null) this.bankName = dto.getBankName();
        if (dto.getDepositor() != null) this.depositor = dto.getDepositor();

        if (dto.getSchedule() != null) this.schedule = dto.getSchedule();
        if (dto.getRuntime() != null) this.runtime = dto.getRuntime();
        if (dto.getAccount() != null) this.account = dto.getAccount();
        if (dto.getContact() != null) this.contact = dto.getContact();
        if (dto.getHashtag() != null) this.hashtag = dto.getHashtag();
        if (dto.getSummary() != null) this.summary = dto.getSummary();
    }

    public void increaseSoldTicket(int quantity) {
        if (this.totalSoldTicket == null) {
            this.totalSoldTicket = 0;
        }
        this.totalSoldTicket += quantity;
    }

    public void updatePosterImageUrl(String imageUrl){
        this.posterImageUrl = imageUrl;
    }


    public void reviseShowInfo(String hashtag, String summary, String account, String contact) {
            this.hashtag = hashtag;
            this.summary = summary;
            this.account = account;
            this.contact = contact;
    }

    public void approve(){
        this.status = AmateurShowStatus.APPROVED_ONGOING;
    }
    public void reject(String rejectReason){
        this.status = AmateurShowStatus.REJECTED;
        this.rejectReason = rejectReason;

    }
}
