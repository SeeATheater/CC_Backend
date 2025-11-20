package cc.backend;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.HotBoard;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.HotBoardRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotBoardDataInitializer implements CommandLineRunner {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final HotBoardRepository hotBoardRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이미 핫게시판 데이터가 있으면 스킵
        if (hotBoardRepository.count() > 0) {
            log.info("핫게시판 데이터가 이미 존재합니다.");
            return;
        }

        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트용 회원이 없습니다."));

        // 좋아요 10개인 게시글 생성
        Board board = Board.builder()
                .title("핫게시판 테스트 게시글")
                .content("좋아요 10개 이상인 인기 게시글입니다.")
                .boardType(BoardType.NORMAL)
                .likeCount(10)
                .commentCount(0)
                .member(member)
                .build();

        Board savedBoard = boardRepository.save(board);

        // 핫게시판에 등록
        HotBoard hotBoard = HotBoard.builder()
                .board(savedBoard)
                .hotRegisteredAt(LocalDateTime.now())
                .build();

        hotBoardRepository.save(hotBoard);
        log.info("핫게시판 테스트 데이터 생성 완료: boardId={}", savedBoard.getId());
    }
}

