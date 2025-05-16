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

    //TODO : 추후 Auth로 변환
    @PostMapping
    public BoardResponse createBoard(@RequestParam Long memberId, @RequestBody BoardRequest request) {
        return boardService.createBoard(memberId, request);
    }
}
