package cc.backend.photoAlbum.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.service.PhotoAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "사진첩")
@RequestMapping("/photoAlbums")
public class PhotoAlbumController {
    private final PhotoAlbumService photoAlbumService;

    @GetMapping("/{photoAlbumId}")
    @Operation(summary = "사진첩 단건 조회 API", description = "공연별 사진첩을 단건 조회하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> getPhotoAlbum(
            @PathVariable("photoAlbumId") Long photoAlbumId,
            @Parameter Long memberId) {
        return ApiResponse.onSuccess(photoAlbumService.getPhotoAlbum(photoAlbumId, memberId));
    }


    @PostMapping("")
    @Operation(summary = "사진첩 등록 API", description = "사진첩을 등록하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> uploadPhotoAlbum(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "imageRequestDTOs에는 presigned urls 호출로 받은 keyName, publicUrls 값만 전달")
    @RequestBody PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO,
            @Parameter Long memberId){
        return ApiResponse.onSuccess(photoAlbumService.createPhotoAlbum(requestDTO, memberId));
    }

    @GetMapping("/member/{memberId}") //로그인 구현 시 수정
    @Operation(summary = "등록자 계정의 전체 사진첩 피드 조회 API", description = "등록자의 사진첩 피드를 전체 조회하는 API 입니다.")
    public ApiResponse<List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO>> getPhotoAlbumList(
            @Parameter Long memberId){ //로그인 구현 시 토큰으로 받도록 수정
        return ApiResponse.onSuccess(photoAlbumService.getPhotoAlbumList(memberId));
    }
}
