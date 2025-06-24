package cc.backend.image.DTO;

import cc.backend.image.FilePath;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



public class ImageRequestDTO {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartialImageRequestDTO {
        private String keyName;
        private String imageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FullImageRequestDTO {
        private String keyName;
        private String imageUrl;
        private FilePath filePath;
        private Long contentId;
        private Long memberId;
    }

}
