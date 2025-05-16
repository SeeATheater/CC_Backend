package cc.backend.service;

import cc.backend.dto.request.BoardRequest;
import cc.backend.dto.response.BoardResponse;
import cc.backend.entity.Board;
import cc.backend.entity.Member;
import cc.backend.repository.BoardRepository;
import cc.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    // 게시글 작성
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
                .build();
    }

}