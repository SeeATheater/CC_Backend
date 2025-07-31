package cc.backend.member.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.board.entity.Board;
import cc.backend.domain.common.BaseEntity;
import cc.backend.member.enumerate.ActiveStatus;
import cc.backend.member.enumerate.Role;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.ticket.entity.MemberTicket;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    private String username;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String address;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private String birth_date;

    private String gender;

    private String password;

    private String delivery_address;

    private String inactive_date;

    @Enumerated(EnumType.STRING)
    private ActiveStatus active_status;

    @Column(name = "kakao_id")
    private String kakaoId;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards;

    @OneToMany (mappedBy = "member", cascade = CascadeType.ALL)
    private List<AmateurShow> amateurShows = new ArrayList<>();

    @OneToMany (mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberNotice> memberNotices = new ArrayList<>();

    @OneToMany (mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberTicket> memberTickets = new ArrayList<>();

    @Builder
    public Member(String username, String name, Role role, String address, String email, String phone,
                       String birth_date, String gender, String password, String delivery_address, String inactive_date,String kakaoId) {
        this.username = username;
        this.name = name;
        this.role = role;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.birth_date = birth_date;
        this.gender = gender;
        this.password = password;
        this.delivery_address = delivery_address;
        this.kakaoId = kakaoId;
        this.inactive_date = inactive_date;
        this.active_status = ActiveStatus.ACTIVE;
    }



    public void deactivateMember(Member member) {
        this.active_status = ActiveStatus.INACTIVE;
    }
    public void reactivateMember(Member member) {
        this.active_status = ActiveStatus.ACTIVE;
    }

    // --공연진 좋아요--
    @OneToMany(mappedBy = "liker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLike> likesGiven = new ArrayList<>();

    @OneToMany(mappedBy = "performer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLike> likesReceived = new ArrayList<>();

    public void updateKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public void updateNickname(String nickname) {
        this.name = nickname; //카카오의 닉네임 == CC 이름
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
    }

}
