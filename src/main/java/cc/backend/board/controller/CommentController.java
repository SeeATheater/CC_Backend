package cc.backend.board.controller;

import cc.backend.board.dto.request.CommentRequest;
import cc.backend.board.dto.response.CommentCreateResponse;
import cc.backend.board.dto.response.CommentResponse;
import cc.backend.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "댓글")
@RequestMapping("/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글/대댓글 생성
    @Operation(
            summary = "댓글/대댓글 생성 API",
            description = "댓글 또는 대댓글을 작성합니다. parentCommentId가 null이면 댓글, 아니면 대댓글입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글/대댓글 생성 성공",
                            content = @Content(schema = @Schema(implementation = CommentCreateResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<CommentCreateResponse> createComment(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "댓글 생성 요청 DTO", required = true)
            @RequestBody CommentRequest request)  {
        CommentCreateResponse response = commentService.createComment(boardId,request);
        return ResponseEntity.ok(response);
    }

    // 댓글/대댓글 수정
    @Operation(
            summary = "댓글/대댓글 수정 API",
            description = "댓글 또는 대댓글을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글/대댓글 수정 성공",
                            content = @Content(schema = @Schema(implementation = CommentCreateResponse.class)))
            }
    )
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentCreateResponse> updateComment(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
            @Parameter(description = "회원 ID", required = true) @RequestParam Long memberId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "수정할 댓글 내용", required = true)
            @RequestBody String newContent){
        CommentCreateResponse response = commentService.updateComment(memberId, commentId, newContent);
        return ResponseEntity.ok(response);
    }

    // 댓글/대댓글 삭제
    @Operation(
            summary = "댓글/대댓글 삭제 API",
            description = "댓글 또는 대댓글을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글/대댓글 삭제 성공")
            }
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
            @Parameter(description = "회원 ID", required = true) @RequestParam Long memberId)  {
        commentService.deleteComment(memberId, commentId);
        return ResponseEntity.ok().build();
    }

    // 댓글/대댓글 트리형 전체 조회
    @Operation(
            summary = "댓글/대댓글 전체 조회 API",
            description = "해당 게시글의 모든 댓글/대댓글을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponse.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId) {
        List<CommentResponse> comments = commentService.getComments(boardId);
        return ResponseEntity.ok(comments);
    }

    //댓글 좋아요
    @Operation(
            summary = "댓글/대댓글 좋아요 토글 API ",
            description = "댓글 또는 대댓글에 좋아요/좋아요 취소(토글)를 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요/취소 결과 (1: 좋아요, -1: 취소)")
            }
    )
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Integer> toggleCommentLike(
                                                     @Parameter(description = "댓글 ID", required = true) @PathVariable Long commentId,
                                                     @Parameter(description = "회원 ID", required = true) @RequestParam Long memberId) {
        int result = commentService.toggleCommentLike(commentId, memberId);
        return ResponseEntity.ok(result);
    }

}