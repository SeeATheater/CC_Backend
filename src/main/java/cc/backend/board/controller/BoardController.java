package cc.backend.board.controller;

import cc.backend.board.dto.request.BoardRequest;
import cc.backend.board.dto.request.BoardSearchRequest;
import cc.backend.board.dto.response.BoardDetailResponse;
import cc.backend.board.dto.response.BoardResponse;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.board.service.BoardService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@Tag(name = "게시판")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;


    @Operation(summary = "게시글 작성 API", description = "게시글을 등록합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 등록 성공",
            content = @Content(schema = @Schema(implementation = BoardResponse.class)))
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@Parameter(description = "작성자 회원 ID", required = true) @AuthenticationPrincipal(expression = "member") Member member,
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "게시글 요청 DTO", required = true)
                                                     @RequestBody BoardRequest request) {
        BoardResponse response= boardService.createBoard(member.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 수정 API", description = "게시글을 수정합니다.")
    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(@Parameter(description = "작성자 회원 ID", required = true) @AuthenticationPrincipal(expression = "member") Member member,
                                                     @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
                                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "게시글 요청 DTO", required = true)@RequestBody BoardRequest request)
    {
        BoardResponse response = boardService.updateBoard(member.getId(), boardId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 삭제 API", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @Parameter(description = "작성자 회원 ID", required = true) @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId) {
        boardService.deleteBoard(member.getId(),boardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 목록 조회 API", description = "게시글을 무한 스크롤 방식으로 조회합니다.")
    @GetMapping
    public Slice<BoardDetailResponse> getBoards(
            @Parameter(description = "게시판 타입", required = true) @RequestParam BoardType boardType,
            @Parameter(description = "페이지 번호(0부터 시작)", required = true) @RequestParam int page,
            @Parameter(description = "페이지 크기", required = true) @RequestParam int size
    ) {
        return boardService.getBoards(boardType, page, size);
    }

    @Operation(summary = "게시글 상세 조회 API", description = "게시글 상세 정보를 조회합니다.")
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoard(
                                                        @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId){
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }

    @Operation(summary = "게시글 좋아요 API", description = "게시글 좋아요를 토글합니다. (누르면 좋아요/다시 누르면 취소)")
    @PostMapping("/{boardId}/like")
    public ResponseEntity<?> toggleLike(
            @Parameter(description = "게시글 ID", required = true) @PathVariable Long boardId,
            @Parameter(description = "회원 ID", required = true) @AuthenticationPrincipal(expression = "member") Member member) {
        int result = boardService.toggleLike(boardId, member.getId());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "핫게시판 조회 API", description = "핫게시판(좋아요 10개 이상) 목록을 조회합니다.")
    @GetMapping("/hot")
    public ResponseEntity<List<BoardDetailResponse>> getHotBoards(){
        List<BoardDetailResponse> hotboards = boardService.getHotBoards();
        return ResponseEntity.ok(hotboards);
    }

    @Operation(summary = "게시글 검색", description = "게시판 타입별로 검색합니다. 일반게시판은 제목+내용, 홍보게시판은 제목+내용+작성자를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<Slice<BoardDetailResponse>> searchBoards(
            @Parameter(description = "검색 키워드", example = "검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "게시판 타입", required = true) @RequestParam BoardType boardType,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {

        BoardSearchRequest request = BoardSearchRequest.builder()
                .keyword(keyword)
                .boardType(boardType)
                .page(page)
                .size(size)
                .build();

        Slice<BoardDetailResponse> result = boardService.searchBoards(request);
        return ResponseEntity.ok(result);
    }
}
