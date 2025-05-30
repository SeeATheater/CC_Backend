package cc.backend.board.repository;

import cc.backend.board.entity.Comment;
import cc.backend.board.entity.CommentLike;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);
    long countByComment(Comment comment);
}
