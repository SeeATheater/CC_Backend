package cc.backend.photoAlbum.dto;

import cc.backend.image.DTO.ImageResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class PhotoAlbumResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoAlbumResultDTO {
        private String performerName;
        private Long photoAlbumId;
        private Long amateurShowId;
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
    public static class PhotoAlbumResultWithPresignedUrlDTO {
        private Long photoAlbumId;
        private String performerName;
        private Long amateurShowId;
        private String amateurShowName;
        private String schedule;
        private String detailAddress;
        private String content;
        private List<ImageResponseDTO.ImageResultWithPresignedUrlDTO> imageResultWithPresignedUrlDTOs;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SinglePhotoAlbumDTO {
        private Long photoAlbumId;
        private Long amateurShowId;
        private String amateurShowName;
        private String performerName;
        private String detailAddress;
        private ImageResponseDTO.ImageResultWithPresignedUrlDTO imageResultWithPresignedUrlDTO;
    }


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberPhotoAlbumDTO {
        private Long photoAlbumId;
        private Long memberId;
        private Long amateurShowId;
        private String performerName;
        private String amateurShowName;
        private String imageUrl;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScrollMemberPhotoAlbumDTO { //커서 기반
        private List<MemberPhotoAlbumDTO> photoAlbumDTOs;
        private boolean hasNext;
        private Long nextCursorId;
        private LocalDateTime nextCursorUpdatedAt;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyShowsForPhotoAlbumDTO {
        private Long memberId;
        private Long amateurShowId;
        private String amateurShowName;
        private String schedule;
    }


}
