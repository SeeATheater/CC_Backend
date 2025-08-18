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
        private String performerName;
        private Long photoAlbumId;
        private String amateurShowName;
        private String schedule;
        private String detailAddress;
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
        private String performerName;
        private String detailAddress;
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformerPhotoAlbumDTO {
        private List<SinglePhotoAlbumDTO> singlePhotoAlbumDTOs;
        private String performerName;
        private Integer number;
        private boolean hasNext;
        private Long nextCursor;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberPhotoAlbumDTO {
        private Long photoAlbumId;
        private Long memberId;
        private String performerName;
        private String amateurShowName;
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScrollMemberPhotoAlbumDTO {
        private List<MemberPhotoAlbumDTO> photoAlbumDTOs;
        private boolean hasNext;
        private Long nextCursor;
    }



}
