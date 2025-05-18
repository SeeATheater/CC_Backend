package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.common.entity.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AmateurShow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    private String name;

    private String place;

    private String schedule;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany (mappedBy = "amateurShow", cascade = CascadeType.ALL)
    private List<PhotoAlbum> photoAlbums = new ArrayList<>();

    @Builder
    public AmateurShow(String name, String place, String schedule, List<PhotoAlbum> photoAlbums) {
        this.name = name;
        this.place = place;
        this.schedule = schedule;
        this.photoAlbums = photoAlbums;
    }
}
