package cc.backend.photoAlbum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class PhotoAlbumRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePhotoAlbumDTO {

        private Long amateurShowId;
        private String title;
        private String content;
        private List<String> imageUrls;
    }
}
