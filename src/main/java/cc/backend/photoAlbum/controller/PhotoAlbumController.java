package cc.backend.photoAlbum.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.service.PhotoAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "사진첩")
@RequestMapping("/photoAlbums")
public class PhotoAlbumController {
    private final PhotoAlbumService photoAlbumService;

    @GetMapping("/{photoAlbumId}")
    @Operation(summary = "사진첩 단건 조회 API", description = "사진첩을 개별 조회하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> getPhotoAlbum(@PathVariable("photoAlbumId") Long photoAlbumId) {
        return ApiResponse.onSuccess(photoAlbumService.getPhotoAlbum(photoAlbumId));
    }


    @PostMapping("")
    @Operation(summary = "사진첩 등록 API", description = "사진첩을 등록하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> getPhotoAlbum(@RequestBody PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO){
        return ApiResponse.onSuccess(photoAlbumService.createPhotoAlbum(requestDTO));
    }

    @GetMapping("/member/{memberId}") //로그인 구현 시 수정
    @Operation(summary = "등록자 계정의 사진첩 피드 조회 API", description = "등록자의 사진첩 피드를 조회하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumListDTO> getPhotoAlbumList(@PathVariable Long memberId){ //로그인 구현 시 토큰으로 받도록 수정
        return ApiResponse.onSuccess(photoAlbumService.getPhotoAlbumList(memberId));
    }
}
