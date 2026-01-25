package cc.backend.admin.photoAlbum.controller;

import cc.backend.admin.photoAlbum.dto.AdminPhotoAlbumResponseDTO;
import cc.backend.admin.photoAlbum.service.AdminPhotoAlbumService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.apiPayLoad.SliceResponse;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.member.entity.Member;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.photoAlbum.service.PhotoAlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/photoAlbum")
@RequiredArgsConstructor
@Tag(name = "관리자 사진첩 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPhotoAlbumController {
    private final AdminPhotoAlbumService adminPhotoAlbumService;

    @GetMapping("")
    @Operation(summary = "관리자페이지 사진첩 전체 조회 API", description = "전체 사진첩 업로드 날짜 내림차순 정렬")
    public ApiResponse<PageResponse<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO>> getAllPhotoAlbum(
            @ParameterObject Pageable pageable){
        return ApiResponse.onSuccess(adminPhotoAlbumService.getAllPhotoAlbum(pageable));
    }

    @GetMapping("/{photoAlbumId}")
    @Operation(summary = "관리자페이지 사진첩 상세 조회 API", description = "개별 사진첩 상세 조회")
    public ApiResponse<AdminPhotoAlbumResponseDTO.DetailPhotoAlbumDTO> getPhotoAlbum(@PathVariable Long photoAlbumId){
        return ApiResponse.onSuccess(adminPhotoAlbumService.getPhotoAlbumDetail(photoAlbumId));
    }

    @DeleteMapping("/{photoAlbumId}")
    @Operation(summary = "관리자페이지 사진첩 내리기 API", description = "개별 사진첩 삭제")
    public ApiResponse<String> deletePhotoAlbum(@PathVariable Long photoAlbumId){
        return ApiResponse.onSuccess(adminPhotoAlbumService.deletePhotoAlbum(photoAlbumId));
    }

    @GetMapping("/search")
    @Operation(summary = "관리자페이지 사진첩 검색 API", description = "사진첩 id, 공연 id, 공연제목, 사진첩 내용에 키워드를 포함하면 반환")
    public ApiResponse<SliceResponse<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO>> searchPhotoAlbum(
            @RequestParam String keyword,
            @ParameterObject Pageable pageable){
        return ApiResponse.onSuccess(SliceResponse.of(adminPhotoAlbumService.searchPhotoAlbum(keyword, pageable)));
    }



}
