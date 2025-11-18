package cc.backend.image.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.config.s3.S3Service;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.service.ImageService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "이미지")
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    // 이미지 저장 - DB에 저장
    @Operation(summary = "Image 한 개 저장", description = "s3 url 요청, PUT 이후 DB 저장 위해 호출 ")
    @PostMapping("")
    public ApiResponse<ImageResponseDTO.ImageResultWithPresignedUrlDTO> saveImage( @AuthenticationPrincipal(expression = "member") Member member,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "s3 url 요청 후 받은 keyName, imageUrl 그대로 imageRequestDTO로 전달")
            @RequestBody ImageRequestDTO.FullImageRequestDTO requestDTO) {
        return ApiResponse.onSuccess(imageService.saveImage(member.getId(), requestDTO));
    }

    // 다중 이미지 저장
    @Operation(summary = "Image 여러 개 저장", description = " ")
    @PostMapping("/multipleImages")
    public ApiResponse<List<ImageResponseDTO.ImageResultWithPresignedUrlDTO>> saveMultipleImages( @AuthenticationPrincipal(expression = "member") Member member,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "uploadUrls에 PUT 이후 받은 keyName, imageUrl 그대로 imageRequestDTO로 전달")
            @RequestBody List<ImageRequestDTO.FullImageRequestDTO> requestDTOs){
        List<ImageResponseDTO.ImageResultWithPresignedUrlDTO> savedImages = imageService.saveImages(member.getId(),requestDTOs);

        return ApiResponse.onSuccess(savedImages);
    }

    // 이미지 삭제 요청
    @Operation(summary = "DB에서 이미지 삭제", description = "이미지 삭제 api ")
    @DeleteMapping("/delete/{imageId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long imageId,
                                         @AuthenticationPrincipal(expression = "member") Member member) {
        imageService.deleteImage(imageId, member.getId());
        return ApiResponse.onSuccess(null);
    }

    // 이미지 조회
    @Operation(summary = "DB에서 이미지 조회", description = "KeyName으로 이미지 조회용 url 발급 api ")
    @GetMapping("/{keyName}")
    public ApiResponse<ImageResponseDTO.ImageResultWithPresignedUrlDTO> getImage(@PathVariable String keyName,
                                                                 @AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(imageService.getImage(keyName, member.getId()));
    }

    // 이미지 조회
    @Operation(summary = "DB에서 amateurShow 이미지 조회", description = "KeyName으로 포스터, 알림 이미지 조회용 url 발급 api ")
    @GetMapping("/show/{keyName}")
    public ApiResponse<ImageResponseDTO.ImageResultWithPresignedUrlDTO> getPosterImage(@PathVariable String keyName,
                                                                                 @AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(imageService.getPosterImage(keyName, member.getId()));
    }

}
