package cc.backend.photoAlbum.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoAlbum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    @ElementCollection
    @CollectionTable(name = "image_urls", joinColumns = @JoinColumn(name = "photo_album_id"))
    private List<String> imageUrls;

    private String content;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "amateur_show_id")
    private AmateurShow amateurShow;


    @Builder
    public PhotoAlbum(List<String> imageUrls, String content, String title, AmateurShow amateurShow) {
        this.imageUrls = imageUrls;
        this.content = content;
        this.title = title;
        this.amateurShow = amateurShow;
    }
}
