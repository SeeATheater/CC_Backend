package cc.backend.image.DTO;

import cc.backend.image.FilePath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ImageResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageResultDTO {
        private Long id;
        private String keyName;
        private FilePath filePath;
        private Long contentId;
        private LocalDateTime uploadedAt;
        private Long memberId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageResultWithPresignedUrlDTO {
        private Long id;
        private String keyName;
        private String presignedUrl;
        private FilePath filePath;
        private Long contentId;
        private LocalDateTime uploadedAt;
        private Long memberId;
    }
}
