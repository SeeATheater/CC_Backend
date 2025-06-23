package cc.backend.memberLike.repository;

import cc.backend.member.entity.Member;
import cc.backend.memberLike.entity.MemberLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberLikeRepository extends JpaRepository<MemberLike, Long> {
    boolean existsByLikerAndPerformer(Member liker, Member performer);

    Optional<MemberLike> findByLikerAndPerformer(Member liker, Member performer);

    List<MemberLike> findByPerformerId(Long performerId);
}
