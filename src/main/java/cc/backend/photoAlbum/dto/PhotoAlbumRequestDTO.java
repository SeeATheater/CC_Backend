package cc.backend.photoAlbum.dto;

import cc.backend.image.DTO.ImageRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PhotoAlbumRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePhotoAlbumDTO {
        private Long amateurShowId;
        private String content;
        private List<ImageRequestDTO.PartialImageRequestDTO> imageRequestDTOs;
    }
}
