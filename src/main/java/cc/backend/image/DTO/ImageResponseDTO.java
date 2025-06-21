package cc.backend.image.DTO;

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
        private String  imageUrl;
        private Long userId;
        private LocalDateTime uploadedAt;
    }
}
