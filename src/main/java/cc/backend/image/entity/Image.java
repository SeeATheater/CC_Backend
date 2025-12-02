package cc.backend.image.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.board.entity.Board;
import cc.backend.image.FilePath;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_file_content", columnList = "file_path, content_id"),
                @Index(name = "idx_uploaded_at", columnList = "uploaded_at")
        }
)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // S3 객체 키 (예: "board/1.png") - 삭제나 조회 시 필요(객체 url에서 버킷 url 뺀 나머지 = filepath + 파일이름)
    private String keyName;

    // 외부에 공개할 수 있는 URL (예: https://bucket.s3.region.amazonaws.com/board/1.png)
    private String imageUrl;

    // 버킷 내 디렉토리 경로 (board, photoAlbum)
    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    private FilePath filePath;

    // board 와 photoAlbum 각각의 id (매핑없이 식별용)
    @Column(name = "content_id", nullable = false)
    private Long contentId;

    // 업로드 시간
    private LocalDateTime uploadedAt;

    // 작성자
    private Long memberId;

    public Image(String keyName, String imageUrl, FilePath filePath, Long contentId, LocalDateTime uploadedAt, Long memberId) {
        this.keyName = keyName;
        this.imageUrl = imageUrl;
        this.filePath = filePath;
        this.contentId = contentId;
        this.uploadedAt = uploadedAt;
        this.memberId = memberId;
    }

}
