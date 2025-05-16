package cc.backend.controller;

import cc.backend.dto.request.BoardRequest;
import cc.backend.dto.response.BoardResponse;
import cc.backend.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    //TODO : 추후 @AuthenticationPrincipal 로 변환
    @PostMapping
    public BoardResponse createBoard(@RequestParam Long memberId, @RequestBody BoardRequest request) {
        return boardService.createBoard(memberId, request);
    }

    @PutMapping("/{boardId}")
    public BoardResponse updateBoard(@PathVariable Long boardId, @RequestBody BoardRequest request)
    {
        return boardService.updateBoard(boardId, request);
    }

    @DeleteMapping("/{boardId}")
    public void deleteBoard(@PathVariable Long boardId)
    {
        boardService.deleteBoard(boardId);
    }
}
