package cc.backend.notice.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.notice.dto.NoticeResponseDTO;
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

    @PostMapping("hot/{boardId}")
    @Operation(summary = "핫게 선정 알림 API", description = "핫게 선정 알려주는 API 입니다.")
    public ApiResponse<NoticeResponseDTO.BoardNoticeResultDTO> notifyHotBoard(@PathVariable Long boardId){
        return ApiResponse.onSuccess(noticeService.notifyHotBoard(boardId));
    }

    @PostMapping("comment/{boardId}")
    @Operation(summary = "게시글 댓글 알림 API", description = "내가 쓴 게시글에 댓글달리면 알려주는 API 입니다.")
    public ApiResponse<NoticeResponseDTO.BoardNoticeResultDTO> notifyCommentBoard(@PathVariable Long boardId){
        return ApiResponse.onSuccess(noticeService.notifyCommentBoard(boardId));
    }




}
