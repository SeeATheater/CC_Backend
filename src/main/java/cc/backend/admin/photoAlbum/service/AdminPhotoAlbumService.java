package cc.backend.admin.photoAlbum.service;

import cc.backend.admin.photoAlbum.dto.AdminPhotoAlbumResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import cc.backend.image.service.ImageService;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.repository.PhotoAlbumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPhotoAlbumService {
    private final PhotoAlbumRepository photoAlbumRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public Page<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO> getAllPhotoAlbum(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<PhotoAlbum> photoAlbums = photoAlbumRepository.findAll(pageable);

        return photoAlbums.map(photoAlbum -> AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO.builder()
                .id(photoAlbum.getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .uploaderId(photoAlbum.getAmateurShow().getMember().getId())
                .uploaderName(photoAlbum.getAmateurShow().getMember().getName())
                .updatedAt(photoAlbum.getUpdatedAt())
                .build());
    }

    @Transactional(readOnly = true)
    public AdminPhotoAlbumResponseDTO.DetailPhotoAlbumDTO getPhotoAlbumDetail(Long photoAlbumId){
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbumId);

        List<ImageResponseDTO.ImageResultDTO> imageResultDTOs = images.stream()
                .map(image -> ImageResponseDTO.ImageResultDTO.builder()
                        .id(image.getId())
                        .keyName(image.getKeyName())
                        .filePath(image.getFilePath())
                        .contentId(image.getContentId())
                        .uploadedAt(image.getUploadedAt())
                        .build()).toList();

        return AdminPhotoAlbumResponseDTO.DetailPhotoAlbumDTO.builder()
                .photoAlbumId(photoAlbum.getId())
                .amateurShowId(photoAlbum.getAmateurShow().getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .content(photoAlbum.getContent())
                .uploaderId(photoAlbum.getAmateurShow().getMember().getId())
                .uploaderName(photoAlbum.getAmateurShow().getMember().getName())
                .updatedAt(photoAlbum.getUpdatedAt())
                .imageResultDTOs(imageResultDTOs)
                .build();
    }

    @Transactional
    public String deletePhotoAlbum(Long photoAlbumId) {
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        Member member = photoAlbum.getAmateurShow().getMember();

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbumId);
        images.forEach(image -> imageService.deleteImage(image.getId(), member.getId() ));

        photoAlbumRepository.delete(photoAlbum);

        return "관리자 권한으로 삭제가 완료되었습니다.";
    }

    @Transactional(readOnly = true)
    public List<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO> searchPhotoAlbum(String keyword){
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<PhotoAlbum> results = photoAlbumRepository.searchPhotoAlbumByKeyword(keyword);
        return results.stream()
                .map(photoAlbum -> AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO.builder()
                        .id(photoAlbum.getId())
                        .amateurShowName(photoAlbum.getAmateurShow().getName())
                        .uploaderId(photoAlbum.getAmateurShow().getMember().getId())
                        .uploaderName(photoAlbum.getAmateurShow().getMember().getName())
                        .updatedAt(photoAlbum.getUpdatedAt())
                        .build())
                .toList();
    }
}
