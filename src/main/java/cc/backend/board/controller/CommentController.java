package cc.backend.board.controller;

import cc.backend.board.dto.request.CommentRequest;
import cc.backend.board.dto.response.CommentCreateResponse;
import cc.backend.board.dto.response.CommentResponse;
import cc.backend.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardId}/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글/대댓글 생성
    @PostMapping
    public ResponseEntity<CommentCreateResponse> createComment(
            @PathVariable Long boardId,
            @RequestBody CommentRequest request) {
        CommentCreateResponse response = commentService.createComment(boardId,request);
        return ResponseEntity.ok(response);
    }

    // 댓글/대댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentCreateResponse> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestParam Long memberId,
            @RequestBody String newContent) {
        CommentCreateResponse response = commentService.updateComment(memberId, commentId, newContent);
        return ResponseEntity.ok(response);
    }

    // 댓글/대댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @RequestParam Long memberId) {
        commentService.deleteComment(memberId, commentId);
        return ResponseEntity.ok().build();
    }

    // 댓글/대댓글 트리형 전체 조회
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long boardId) {
        List<CommentResponse> comments = commentService.getComments(boardId);
        return ResponseEntity.ok(comments);
    }
}