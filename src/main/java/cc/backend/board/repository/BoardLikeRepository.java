package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardLike;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    boolean existsByMemberAndBoard(Member member, Board board);
    Optional<BoardLike> findByMemberAndBoard(Member member, Board board);
    long countByBoard(Board board);
}