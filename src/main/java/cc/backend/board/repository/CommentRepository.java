package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardOrderByCreatedAtAsc(Board board);
}
