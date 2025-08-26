package cc.backend.admin.photoAlbum.dto;

import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.notice.entity.enums.NoticeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자용 사진첩 관리 응답 DTO")
public class AdminPhotoAlbumResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimplePhotoAlbumDTO {
        private Long id;
        private String amateurShowName;
        private Long uploaderId;
        private String uploaderName;
        private LocalDateTime updatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailPhotoAlbumDTO {
        private Long photoAlbumId;
        private Long amateurShowId;
        private String amateurShowName;
        private Long uploaderId;
        private String uploaderName;
        private String content;
        private LocalDateTime updatedAt;
        private List<ImageResponseDTO.ImageResultDTO> imageResultDTOs;

    }


}
