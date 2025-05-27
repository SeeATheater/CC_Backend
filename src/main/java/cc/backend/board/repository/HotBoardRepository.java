package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.HotBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HotBoardRepository extends JpaRepository<HotBoard, Long> {
    List<HotBoard> findTop10ByOrderByHotRegisteredAtAsc();
    List<HotBoard> findTop10ByOrderByBoard_CreatedAtDesc();
    Optional<HotBoard> findByBoard(Board board);
    long count();
}
