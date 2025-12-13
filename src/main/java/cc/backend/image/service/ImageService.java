package cc.backend.image.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.config.s3.S3Service;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    /**
     * 프론트에서 S3 업로드 완료 후 호출
     * userId 업로드한 사용자 ID
     * keyName S3 객체 키 (ex: "images/uuid.png")
     * imageUrl 공개 접근 가능한 URL
     * return ImageResultDTO
     */
    @Transactional
    public ImageResponseDTO.ImageResultWithPresignedUrlDTO saveImage(Long memberId, ImageRequestDTO.FullImageRequestDTO requestDTO) {

        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //S3에 실제 존재하는 이미지인지 검증
        if(!s3Service.doesObjectExist(requestDTO.getKeyName(), memberId)) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_IN_S3);
        }

        Image image = Image.builder()
                .keyName(requestDTO.getKeyName())
                .filePath(requestDTO.getFilePath())
                .contentId(requestDTO.getContentId())
                .uploadedAt(LocalDateTime.now())
                .memberId(memberId)
                .build();

        Image newImage = imageRepository.save(image);

        return getImage(newImage.getKeyName(), memberId);
    }

    @Transactional
    public ImageResponseDTO.ImageResultWithPresignedUrlDTO saveImageWithImageUrl(Long memberId, ImageRequestDTO.FullImageRequestDTO requestDTO, Optional<String> imageUrlOpt) {

        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //S3에 실제 존재하는 이미지인지 검증
        if(!s3Service.doesObjectExist(requestDTO.getKeyName(), memberId)) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_IN_S3);
        }

        String imageUrl = imageUrlOpt.orElse("");
        Image image = Image.builder()
                .keyName(requestDTO.getKeyName())
                .filePath(requestDTO.getFilePath())
                .contentId(requestDTO.getContentId())
                .uploadedAt(LocalDateTime.now())
                .memberId(memberId)
                .imageUrl(imageUrl)
                .build();

        Image newImage = imageRepository.save(image);

        return getImage(newImage.getKeyName(), memberId);
    }

    //다중 이미지 저장
    @Transactional
    public List<ImageResponseDTO.ImageResultWithPresignedUrlDTO> saveImages(Long memberId, List<ImageRequestDTO.FullImageRequestDTO> requestDTOs){
        return requestDTOs.stream()
                .map(requestDTO-> saveImage(memberId, requestDTO))
                .collect(Collectors.toList());
    }

    // 이미지 단건 조회
    public ImageResponseDTO.ImageResultWithPresignedUrlDTO getImage(String keyName, Long memberId){
        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        Image image = imageRepository.findByKeyName(keyName);
        if (image == null) {
            throw new GeneralException(ErrorStatus.IMAGE_NOT_FOUND);
        }

        String presignedUrl = s3Service.createPresignedGetUrl(image.getKeyName(), memberId);

        return ImageResponseDTO.ImageResultWithPresignedUrlDTO.builder()
                .id(image.getId())
                .keyName(image.getKeyName())
                .presignedUrl(presignedUrl)
                .filePath(image.getFilePath())
                .contentId(image.getContentId())
                .uploadedAt(image.getUploadedAt())
                .memberId(image.getMemberId())
                .build();
    }

    public ImageResponseDTO.ImageResultWithPresignedUrlDTO getPosterImage(String keyName, Long memberId){
        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        Image image = imageRepository.findByFilePathAndKeyName(FilePath.amateurShow, keyName);

        String presignedUrl = "";
        if(image == null){
            presignedUrl = "keyName에 해당하는 이미지가 존재하지 않습니다.";
        }
        else {
            presignedUrl = s3Service.createPresignedGetUrl(image.getKeyName(), memberId);
        }

        return ImageResponseDTO.ImageResultWithPresignedUrlDTO.builder()
                .id(image.getId())
                .keyName(image.getKeyName())
                .presignedUrl(presignedUrl)
                .filePath(image.getFilePath())
                .contentId(image.getContentId())
                .uploadedAt(image.getUploadedAt())
                .memberId(image.getMemberId())
                .build();
    }


    public List<ImageResponseDTO.ImageResultWithPresignedUrlDTO> getImages(List<Image> images, Long memberId) {

        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        if (images.isEmpty()) {
            return new ArrayList<>();
        }

        // keyName 기반 presigned URL 일괄 발급
        Map<String, String> presignedUrls = s3Service.createPresignedGetUrls(
                images.stream().map(Image::getKeyName).toList(),
                memberId
        );

        // DTO 변환
        return images.stream()
                .map(img -> ImageResponseDTO.ImageResultWithPresignedUrlDTO.builder()
                        .id(img.getId())
                        .keyName(img.getKeyName())
                        .presignedUrl(presignedUrls.get(img.getKeyName()))  //Map에서 keyName을 key로 조회한 value = presigned Url
                        .filePath(img.getFilePath())
                        .contentId(img.getContentId())
                        .uploadedAt(img.getUploadedAt())
                        .memberId(img.getMemberId())
                        .build()
                )
                .toList();
    }
    // 이미지 삭제-s3 동시 삭제 지원
    @Transactional
    public void deleteImage(Long imageId, Long memberId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.IMAGE_NOT_FOUND));

        // DB에서 이미지 삭제
        imageRepository.delete(image);

        // S3 삭제는 트랜잭션 바깥에서 수행
        try {
            s3Service.deleteFile(image.getKeyName(), memberId);
        } catch (Exception e) {
            // 로깅만 하고 예외 발생은 안 시킴
            log.error("S3 삭제 실패: {}", image.getKeyName(), e);
        }
    }

    @Transactional
    public void updateImage(
            Long memberId,
            String keyName,
            Optional<String> imageUrlOpt,
            Long contentId,
            FilePath filePath
    ) {
        if (keyName == null || keyName.isBlank()) {
            return; // keyName 없으면 처리하지 않음
        }

        String imageUrl = imageUrlOpt.orElse("");

        // 기존 이미지 조회 (filePath + contentId 기준)
        Image existingImage = imageRepository
                .findAllByFilePathAndContentId(filePath, contentId)
                .stream()
                .findFirst()
                .orElse(null);

        // 기존 이미지가 있고, keyName이 다르면 삭제 후 새로 저장
        if (existingImage != null && !existingImage.getKeyName().equals(keyName)) {
            deleteImage(existingImage.getId(), memberId);
        }

        // 새 이미지 저장
        Image image = Image.builder()
                .keyName(keyName)
                .imageUrl(imageUrl)
                .filePath(filePath)
                .contentId(contentId)
                .uploadedAt(LocalDateTime.now())
                .memberId(memberId)
                .build();

        imageRepository.save(image);
    }

}
