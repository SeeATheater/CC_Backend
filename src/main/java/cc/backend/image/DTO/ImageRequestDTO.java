package cc.backend.image.DTO;

import cc.backend.image.FilePath;
import io.swagger.v3.oas.annotations.media.Schema;
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
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FullImageRequestDTO {
        @Schema(description = "S3 객체 키", example = "board/uuid.png")
        private String keyName;

        @Schema(hidden = true)
        private FilePath filePath;

        @Schema(hidden = true)
        private Long contentId;

        @Schema(hidden = true)
        private Long memberId;
    }

}
