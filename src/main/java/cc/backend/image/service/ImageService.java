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
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
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
    public ImageResponseDTO.ImageResultDTO saveImage(Long memberId, ImageRequestDTO.FullImageRequestDTO requestDTO) {

        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        //S3에 실제 존재하는 이미지인지 검증
        if(!s3Service.doesObjectExist(requestDTO.getKeyName(), memberId)) {
            throw new GeneralException(ErrorStatus.NOT_FOUND_IN_S3);
        }

        Image image = Image.builder()
                .keyName(requestDTO.getKeyName())
                .imageUrl(requestDTO.getImageUrl())
                .filePath(requestDTO.getFilePath())
                .contentId(requestDTO.getContentId())
                .uploadedAt(LocalDateTime.now())
                .memberId(requestDTO.getMemberId())
                .build();

        Image newImage = imageRepository.save(image);

        return ImageResponseDTO.ImageResultDTO.builder()
                .id(newImage.getId())
                .keyName(newImage.getKeyName())
                .imageUrl(newImage.getImageUrl())
                .filePath(newImage.getFilePath())
                .contentId(newImage.getContentId())
                .uploadedAt(newImage.getUploadedAt())
                .memberId(newImage.getMemberId())
                .build();
    }

    //다중 이미지 저장
    @Transactional
    public List<ImageResponseDTO.ImageResultDTO> saveImages(Long memberId, List<ImageRequestDTO.FullImageRequestDTO> requestDTOs){
        return requestDTOs.stream()
                .map(requestDTO-> saveImage(memberId, requestDTO))
                .collect(Collectors.toList());
    }

    // 이미지 단건 조회
    public ImageResponseDTO.ImageResultDTO getImage(Long imageId, Long memberId){
        memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        Image image = imageRepository.findById(imageId)
                .orElseThrow(()->new GeneralException(ErrorStatus.IMAGE_NOT_FOUND));
        return ImageResponseDTO.ImageResultDTO.builder()
                .id(image.getId())
                .keyName(image.getKeyName())
                .imageUrl(image.getImageUrl())
                .filePath(image.getFilePath())
                .contentId(image.getContentId())
                .uploadedAt(image.getUploadedAt())
                .memberId(image.getMemberId())
                .build();
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


}
