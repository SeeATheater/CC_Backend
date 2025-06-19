package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardReport;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardReportRepository extends JpaRepository<BoardReport, Long> {
    boolean existsByMemberAndBoard(Member member, Board board);
    long countByBoard(Board board);
}