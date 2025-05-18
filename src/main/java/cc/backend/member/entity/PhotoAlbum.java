package cc.backend.member.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Entity
@Getter
@RequiredArgsConstructor
public class PhotoAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    @ElementCollection
    @CollectionTable(name = "imgUrls", joinColumns = @JoinColumn(name = "member_id"))
    private List<String> imgUrls;

    private String content;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;


    @Builder
    public void PhotoAlbum(List<String> imgUrls, String content, String title) {
        this.imgUrls = imgUrls;
        this.content = content;
        this.title = title;
    }

}
