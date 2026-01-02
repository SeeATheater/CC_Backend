package cc.backend.admin.photoAlbum.controller;

import cc.backend.admin.photoAlbum.dto.AdminPhotoAlbumResponseDTO;
import cc.backend.admin.photoAlbum.service.AdminPhotoAlbumService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Operation(summary = "관리자 사진첩 관리", description = "전체 사진첩 업로드 날짜 내림차순 정렬 및 검색")
    public ApiResponse<SliceResponse<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO>> getAllPhotoAlbum(
            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색 키워드, 공연 명으로 검색", example = "실종")
            @RequestParam(required = false) String keyword
    )
    {
        Slice<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO> slice =
                adminPhotoAlbumService.getPhotoAlbumList(page, size, keyword);

        return ApiResponse.onSuccess(SliceResponse.of(slice));
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

//    @GetMapping("/search")
//    @Operation(summary = "관리자페이지 사진첩 검색 API", description = "사진첩 id, 공연 id, 공연제목, 사진첩 내용에 키워드를 포함하면 반환")
//    public ApiResponse<List<AdminPhotoAlbumResponseDTO.SimplePhotoAlbumDTO>> searchPhotoAlbum(@RequestParam String keyword){
//        return ApiResponse.onSuccess(adminPhotoAlbumService.searchPhotoAlbum(keyword));
//    }



}
