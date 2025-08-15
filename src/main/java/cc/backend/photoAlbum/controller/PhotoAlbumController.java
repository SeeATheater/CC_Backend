package cc.backend.photoAlbum.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.entity.Member;
import cc.backend.photoAlbum.dto.PerformerShowListResponseDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.service.PhotoAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @AuthenticationPrincipal(expression = "member") Member member,
            @PathVariable("photoAlbumId") Long photoAlbumId) {
        return ApiResponse.onSuccess(photoAlbumService.getPhotoAlbum(photoAlbumId, member.getId()));
    }

    @PreAuthorize("hasRole('PERFORMER')")
    @PostMapping("")
    @Operation(summary = "사진첩 등록 API", description = "사진첩을 등록하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> uploadPhotoAlbum(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "imageRequestDTOs에는 presigned urls 호출로 받은 keyName, publicUrls 값만 전달")
    @RequestBody PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO,
            @AuthenticationPrincipal(expression = "member") Member member){
        return ApiResponse.onSuccess(photoAlbumService.createPhotoAlbum(requestDTO, member.getId()));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "등록자 계정의 전체 사진첩 피드 조회 API", description = "등록자의 사진첩 피드를 전체 조회하는 API 입니다.")
    public ApiResponse<List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO>> getPhotoAlbumList(
            @AuthenticationPrincipal(expression = "member") Member member,
            @PathVariable Long memberId){
        return ApiResponse.onSuccess(photoAlbumService.getPhotoAlbumList(member.getId(), memberId));
    }

    @PreAuthorize("hasRole('PERFORMER')")
    @PatchMapping("/{photoAlbumId}")
    @Operation(summary = "사진첩 수정 API", description = "공연별 사진첩을 수정하는 API 입니다.")
    public ApiResponse<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> updatePhotoAlbum(
            @PathVariable("photoAlbumId") Long photoAlbumId,
            @RequestBody PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO,
            @AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(photoAlbumService.updatePhotoAlbum(photoAlbumId,requestDTO, member.getId()));
    }

    @DeleteMapping("/{photoAlbumId}")
    @Operation(summary = "사진첩 삭제 API", description = "공연별 사진첩을 삭제하는 API 입니다.")
    public ApiResponse<String> deletePhotoAlbum(
            @PathVariable("photoAlbumId") Long photoAlbumId,
            @AuthenticationPrincipal(expression = "member") Member member) {
                return ApiResponse.onSuccess(photoAlbumService.deletePhotoAlbum(photoAlbumId, member.getId()));
    }

    @GetMapping("")
    @Operation(summary = "메뉴에서 전체 사진첩 조회 API", description = "최근 올라온 사진첩을 전체 조회하는 API 입니다.")
    public ApiResponse<List<PhotoAlbumResponseDTO.MemberPhotoAlbumDTO>> getAllPhotoAlbum(){
        return ApiResponse.onSuccess(photoAlbumService.getAllPhotoAlbumList());
    }

    @GetMapping("/member/{memberId}/shows")
    @Operation(summary = "특정 공연진의 공연 목록 조회(총 개수 + 리스트)")
    public ApiResponse<PerformerShowListResponseDTO> getPerformerShows(
            @PathVariable
            @Parameter(description = "조회할 공연진 ID", required = true)
            Long memberId,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "페이지(0부터 시작)", example = "0")
            int page,

            @RequestParam(defaultValue = "20")
            @Parameter(description = "페이지 크기", example = "20")
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.onSuccess(
                photoAlbumService.getPerformerShows(memberId, pageable)
        );
    }


}
