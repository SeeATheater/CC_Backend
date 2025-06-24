package cc.backend.notice.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.service.MemberNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "알림")
@RequestMapping("/notice")
public class MemberNoticeController {
    private final MemberNoticeService memberNoticeService;

    @GetMapping("")
    @Operation(summary = "알림 센터 API", description = "내게 온 알림 전체 조회 API 입니다.")
    public ApiResponse<List<MemberNoticeResponseDTO.MemberNoticeDTO>> getAllMemberNotice(@Parameter Long memberId){
        return ApiResponse.onSuccess(memberNoticeService.getAllMemberNotice(memberId));
    }

    @PatchMapping("/{noticeId}")
    @Operation(summary = "알림 읽기 API", description = "내게 온 알림 읽었는지 체크하는 API 입니다.")
    public ApiResponse<MemberNoticeResponseDTO.MemberNoticeDTO> readNotice(@PathVariable Long noticeId, @Parameter Long memberId){
        return ApiResponse.onSuccess(memberNoticeService.readNotice(noticeId, memberId));
    }




}
