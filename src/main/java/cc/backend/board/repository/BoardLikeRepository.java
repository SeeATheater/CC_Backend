package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardLike;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    Optional<BoardLike> findByMemberAndBoard(Member member, Board board);
    boolean existsByMemberIdAndBoardId(Long memberId, Long boardId);
}