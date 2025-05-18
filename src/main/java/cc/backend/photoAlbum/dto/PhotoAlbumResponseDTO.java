package cc.backend.photoAlbum.dto;

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
        private String title;
        private String content;
        private List<String> imageUrls;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoAlbumListDTO {

        private List<PhotoAlbumResultDTO> photoAlbumDTOs;
        private String firstImageUrl;
        private Integer total;
    }

}
