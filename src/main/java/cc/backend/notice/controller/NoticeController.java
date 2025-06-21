package cc.backend.notice.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.service.NoticeService;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "알림")
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService noticeService;

//    @GetMapping("")
//    @Operation(summary = "알림 센터 API", description = "내게 온 알림 조회 API 입니다.")
//    public ApiResponse<MemberNoticeResponseDTO.MemberNoticeDTO> notifyHotBoard(@PathVariable Long boardId){
//        return ApiResponse.onSuccess(noticeService.notifyHotBoard(boardId));
//    }
//
//    @PatchMapping("")
//    @Operation(summary = "알림 읽기 API", description = "내게 온 알림 읽음 표시하는 API 입니다.")
//    public ApiResponse<MemberNoticeResponseDTO.MemberNoticeDTO> notifyCommentBoard(@PathVariable Long boardId){
//        return ApiResponse.onSuccess(noticeService.notifyCommentBoard(boardId));
//    }




}
