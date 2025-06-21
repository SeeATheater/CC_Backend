package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardOrderByCreatedAtAsc(Board board);

    @Query(value = "SELECT * FROM comment WHERE id = :id", nativeQuery = true)
    Optional<Comment> findByIdIncludingDeleted(@Param("id") Long id);
}
