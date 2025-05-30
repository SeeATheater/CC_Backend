package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.enums.BoardType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Slice<Board> findAllByBoardTypeOrderByIdDesc(BoardType boardType, Pageable pageable);
}
