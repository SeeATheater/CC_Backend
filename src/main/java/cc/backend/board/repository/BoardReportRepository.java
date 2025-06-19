package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardReport;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardReportRepository extends JpaRepository<BoardReport, Long> {
    boolean existsByMemberAndBoard(Member member, Board board);

    long countByBoard(Board board);

    @Query("SELECT b FROM Board b WHERE EXISTS (SELECT r FROM BoardReport r WHERE r.board = b)")
    List<Board> findAllReportedBoards();
}