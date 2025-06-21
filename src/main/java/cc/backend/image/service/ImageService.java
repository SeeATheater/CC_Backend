package cc.backend.image.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;

    /**
     * 프론트에서 S3 업로드 완료 후 호출
     * userId 업로드한 사용자 ID
     * keyName S3 객체 키 (ex: "images/uuid.png")
     * imageUrl 공개 접근 가능한 URL
     * return ImageResultDTO
     */
    @Transactional
    public ImageResponseDTO.ImageResultDTO saveImage(ImageRequestDTO requestDTO) {
        String imageUrl = "https://ccbucket-0528.s3.ap-northeast-2.amazonaws.com/" + requestDTO.getKeyName();
        Image image = Image.builder()
                .userId(requestDTO.getUserId())
                .keyName(requestDTO.getKeyName())
                .imageUrl(imageUrl)
                .uploadedAt(LocalDateTime.now())
                .build();

        Image newImage = imageRepository.save(image);

        return ImageResponseDTO.ImageResultDTO.builder()
                .id(newImage.getId())
                .keyName(newImage.getKeyName())
                .imageUrl(newImage.getImageUrl())
                .userId(newImage.getUserId())
                .uploadedAt(newImage.getUploadedAt())
                .build();
    }

    //다중 이미지 저장
    @Transactional
    public List<ImageResponseDTO.ImageResultDTO> saveImages(List<ImageRequestDTO> requestDTOs){
        return requestDTOs.stream()
                .map(this::saveImage)
                .collect(Collectors.toList());
    }


    public ImageResponseDTO.ImageResultDTO getImage(Long imageId){
        Image image = imageRepository.findById(imageId)
                .orElseThrow(()->new GeneralException(ErrorStatus.IMAGE_NOT_FOUND));
        return ImageResponseDTO.ImageResultDTO.builder()
                .id(image.getId())
                .keyName(image.getKeyName())
                .imageUrl(image.getImageUrl())
                .userId(image.getUserId())
                .uploadedAt(image.getUploadedAt())
                .build();
    }

    @Transactional
    public void deleteImage(Long imageId, Consumer<String> s3DeleteFunc) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.IMAGE_NOT_FOUND));

        // S3에서 객체 삭제
        s3DeleteFunc.accept(image.getKeyName());
        // DB에서 이미지 정보 삭제
        imageRepository.delete(image);
    }

}
