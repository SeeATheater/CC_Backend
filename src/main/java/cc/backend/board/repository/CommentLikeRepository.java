package cc.backend.board.repository;

import cc.backend.board.entity.Comment;
import cc.backend.board.entity.CommentLike;
import cc.backend.member.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByMemberAndComment(Member member, Comment comment);

    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.member.id = :memberId AND cl.comment.id IN :commentIds")
    List<Long> findLikedCommentIdsByMemberAndCommentIds(@Param("memberId") Long memberId, @Param("commentIds") List<Long> commentIds);
}
