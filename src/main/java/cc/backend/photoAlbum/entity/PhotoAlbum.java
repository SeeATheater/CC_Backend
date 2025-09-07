package cc.backend.photoAlbum.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.domain.common.BaseEntity;
import cc.backend.image.entity.Image;
import cc.backend.member.entity.Member;
import cc.backend.notice.entity.MemberNotice;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoAlbum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amateur_show_id")
    private AmateurShow amateurShow;

    @Builder
    public PhotoAlbum(String content, AmateurShow amateurShow) {
        this.content = content;
        this.amateurShow = amateurShow;
    }

    public PhotoAlbum updatePhotoAlbum(String content, AmateurShow amateurShow) {
        this.content = content;
        this.amateurShow = amateurShow;
        return this;
    }
}
