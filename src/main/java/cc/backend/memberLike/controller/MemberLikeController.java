package cc.backend.memberLike.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.memberLike.dto.MemberLikeResponseDTO;
import cc.backend.memberLike.service.MemberLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공연진 좋아요")
@RestController
@RequestMapping("/api/member-like")
@RequiredArgsConstructor
public class MemberLikeController {

    private final MemberLikeService memberLikeService;

    // 공연진 좋아요 누르기
    @PostMapping("/{likerId}/like/{performerId}")
    @Operation(summary = "공연진 좋아요 API", description = "사용자가 공연진 좋아요를 누르는 기능입니다.")
    public ApiResponse<MemberLikeResponseDTO> likePerformer(@PathVariable Long likerId, @PathVariable Long performerId) {
        return ApiResponse.onSuccess(memberLikeService.likePerformer(likerId, performerId));
    }

    // 공연진 좋아요 취소
    @DeleteMapping("/{likerId}/like/{performerId}")
    @Operation(summary = "공연진 좋아요 취소 API", description = "사용자가 공연진 좋아요를 취소하는 기능입니다.")
    public ApiResponse<String> cancelLikePerformer(@PathVariable Long likerId, @PathVariable Long performerId) {
        memberLikeService.cancelLikePerformer(likerId, performerId);
        return ApiResponse.onSuccess("좋아요를 취소했습니다.");
    }

    // 좋아요한 공연진 목록 조회
    @GetMapping("/{memberId}/likes")
    @Operation(summary = "좋아요한 공연진 목록 조회", description = "사용자가 좋아요한 공연진 목록을 조회하는 기능입니다.")
    public ApiResponse<List<MemberLikeResponseDTO>> getLikedPerformers(@PathVariable Long memberId) {
        return ApiResponse.onSuccess(memberLikeService.getLikedPerformers(memberId));
    }
}
