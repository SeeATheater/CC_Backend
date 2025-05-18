package cc.backend.member.entity;

import cc.backend.board.entity.Board;
import cc.backend.member.enumerate.ActiveStatus;
import cc.backend.member.enumerate.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    private String username;

    private String name;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String address;

    private String email;

    private String phone;

    private String birth_date;

    private String gender;

    private String password;

    private String delivery_address;

    private String inactive_date;

    @Enumerated(EnumType.STRING)
    private ActiveStatus active_status;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards;

    @OneToMany (mappedBy = "member", cascade = CascadeType.ALL)
    private List<PhotoAlbum> photoAlbums = new ArrayList<>();

    @Builder
    public void Member(String username, String name, Role role, String address, String email, String phone,
                       String birth_date, String gender, String password, String delivery_address, String inactive_date) {
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
        this.inactive_date = inactive_date;
        this.active_status = ActiveStatus.ACTIVE;
    }
}
