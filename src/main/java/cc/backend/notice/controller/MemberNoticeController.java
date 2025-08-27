package cc.backend.notice.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.entity.Member;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.service.MemberNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "알림")
@RequestMapping("/notice")
public class MemberNoticeController {
    private final MemberNoticeService memberNoticeService;

    @GetMapping("")
    @Operation(summary = "알림 센터 API", description = "내게 온 알림 전체 조회 API 입니다. " +
            "응답에서 type이 AMATEURSHOW, REMIND 일 경우 contentId는 AmateurShowId," +
            "type이 HOT, COMMENT 일 경우 contentId는 BoardId," +
            "type이 REPLY 일 경우 contentId는 CommentId" +
            "type이 TICKET 일 경우 contentId는 TicketId")
    public ApiResponse<MemberNoticeResponseDTO.MemberNoticeListDTO> getAllMemberNotice(
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorCreatedAt,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.onSuccess(memberNoticeService.getAllMemberNotice(member.getId(), cursorId, cursorCreatedAt, size));
    }

    @PatchMapping("/{noticeId}")
    @Operation(summary = "알림 읽기 API", description = "내게 온 알림 읽었는지 체크하는 API 입니다." +
            "응답에서 type이 AMATEURSHOW, REMIND 일 경우 contentId는 AmateurShowId," +
            "type이 HOT, COMMENT 일 경우 contentId는 BoardId," +
            "type이 REPLY 일 경우 contentId는 CommentId" +
            "type이 TICKET 일 경우 contentId는 TicketId")
    public ApiResponse<MemberNoticeResponseDTO.MemberNoticeDTO> readNotice(@PathVariable Long noticeId,
                                                                           @AuthenticationPrincipal(expression = "member") Member member){
        return ApiResponse.onSuccess(memberNoticeService.readNotice(noticeId, member.getId()));
    }




}
