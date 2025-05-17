package cc.backend.board.service;

import cc.backend.board.dto.request.BoardRequest;
import cc.backend.board.dto.response.BoardDetailResponse;
import cc.backend.board.dto.response.BoardResponse;
import cc.backend.board.entity.Board;
import cc.backend.member.entity.Member;
import cc.backend.board.repository.BoardRepository;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    // 게시글 작성
    //TODO : 이미지 업로드 코드 추가
    @Transactional
    public BoardResponse createBoard(Long memberId, BoardRequest dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Board board = Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imgUrls(dto.getImgUrls())
                .boardType(dto.getBoardType())
                .likeCount(0)
                .commentCount(0)
                .commentMaxIndex(0)
                .member(member)
                .build();

        boardRepository.save(board);
        return BoardResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(board.getImgUrls())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    //게시글 수정
    @Transactional
   public  BoardResponse updateBoard( Long boardId, BoardRequest dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        board.update(dto.getTitle(), dto.getContent(), dto.getImgUrls(), dto.getBoardType());

        return BoardResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(board.getImgUrls())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();

    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }

    //게시글 조회
    @Transactional(readOnly = true)
    public List<BoardDetailResponse> getBoards() {
        List<Board> boards=boardRepository.findAll();
        return boards.stream()
                .map(BoardDetailResponse::from)
                .collect(Collectors.toList());
    }

    //게시글 상세 조회
    @Transactional(readOnly = true)
    public BoardDetailResponse getBoard(Long boardId) {
        Board board=boardRepository.findById(boardId)
                .orElseThrow(()-> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        return BoardDetailResponse.from(board);
    }

}