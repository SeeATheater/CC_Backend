package cc.backend.image.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // S3 객체 키 (예: "images/uuid.png") - 삭제나 조회 시 필요
    private String keyName;

    // 외부에 공개할 수 있는 URL (예: https://bucket.s3.region.amazonaws.com/images/uuid.png)
    private String imageUrl;

    // 업로드한 사용자 ID (필요 시)
    private Long userId;

    // 업로드 시간
    private LocalDateTime uploadedAt;

    public Image(String keyName, String imageUrl, Long userId, LocalDateTime uploadedAt) {
        this.keyName = keyName;
        this.imageUrl = imageUrl;
        this.userId = userId;
        this.uploadedAt = uploadedAt;
    }
}
