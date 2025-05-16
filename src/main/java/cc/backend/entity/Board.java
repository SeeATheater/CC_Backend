package cc.backend.entity;

import cc.backend.entity.enums.BoardType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType;

    @ElementCollection
    private List<String> imgUrls;

    private int likeCount;
    private int commentCount;
    private int commentMaxIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // ----- method -----
    public void update(String title, String content, List<String> imgUrls, BoardType boardType) {
        this.title = title;
        this.content = content;
        this.imgUrls = imgUrls;
        this.boardType = boardType;
    }
}
