package cc.backend.photoAlbum.dto;

import cc.backend.image.DTO.ImageResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PhotoAlbumResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoAlbumResultDTO {

        private Long photoAlbumId;
        private String amateurShowName;
        private String schedule;
        private String place;
        private String content;
        private List<ImageResponseDTO.ImageResultDTO> imageResultDTOs;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SinglePhotoAlbumDTO {
        private Long photoAlbumId;
        private String amateurShowName;
        private String place;
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberPhotoAlbumDTO {
        private Long photoAlbumId;
        private Long memberId;
        private String memberName;
        private String amateurShowName;
        private String imageUrl;
    }

}
