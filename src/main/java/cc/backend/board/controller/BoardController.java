package cc.backend.board.controller;

import cc.backend.board.dto.request.BoardRequest;
import cc.backend.board.dto.response.BoardDetailResponse;
import cc.backend.board.dto.response.BoardResponse;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //TODO : 추후 @AuthenticationPrincipal 로 변환
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestParam Long memberId, @RequestBody BoardRequest request) {
        BoardResponse response= boardService.createBoard(memberId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(@RequestParam Long memberId,@PathVariable Long boardId, @RequestBody BoardRequest request)
    {
        BoardResponse response = boardService.updateBoard(memberId,boardId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@RequestParam Long memberId,@PathVariable Long boardId)
    {
        boardService.deleteBoard(memberId,boardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/boards")
    public Slice<BoardDetailResponse> getBoards(
            @RequestParam BoardType boardType,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return boardService.getBoards(boardType, page, size);
    }
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoard(@PathVariable Long boardId){
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }
}
