package cc.backend.memberLike.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.entity.Member;
import cc.backend.memberLike.dto.MemberLikeResponseDTO;
import cc.backend.memberLike.service.MemberLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공연진 좋아요")
@RestController
@RequestMapping("/api/member-like")
@RequiredArgsConstructor
public class MemberLikeController {

    private final MemberLikeService memberLikeService;

    // 공연진 좋아요 누르기
    @PostMapping("/like/{performerId}")
    @Operation(summary = "공연진 좋아요 API", description = "사용자가 공연진 좋아요를 누르는 기능입니다.")
    public ApiResponse<MemberLikeResponseDTO> likePerformer(@AuthenticationPrincipal(expression = "member") Member member, @PathVariable Long performerId) {
        return ApiResponse.onSuccess(memberLikeService.likePerformer(member.getId(), performerId));
    }

    // 공연진 좋아요 취소
    @DeleteMapping("/like/{performerId}")
    @Operation(summary = "공연진 좋아요 취소 API", description = "사용자가 공연진 좋아요를 취소하는 기능입니다.")
    public ApiResponse<String> cancelLikePerformer(@AuthenticationPrincipal(expression = "member") Member member, @PathVariable Long performerId) {
        memberLikeService.cancelLikePerformer(member.getId(), performerId);
        return ApiResponse.onSuccess("좋아요를 취소했습니다.");
    }

    // 좋아요한 공연진 목록 조회
    @GetMapping("/likes")
    @Operation(summary = "좋아요한 공연진 목록 조회", description = "사용자가 좋아요한 공연진 목록을 조회하는 기능입니다.")
    public ApiResponse<List<MemberLikeResponseDTO>> getLikedPerformers(@AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(memberLikeService.getLikedPerformers(member.getId()));
    }
}
