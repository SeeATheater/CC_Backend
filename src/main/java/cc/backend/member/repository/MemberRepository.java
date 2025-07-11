package cc.backend.member.repository;

import cc.backend.member.entity.Member;
import cc.backend.notice.entity.MemberNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    @Query("select m from Member m where m.email = :email")
    Optional<Member> findMemberByEmail(@Param("email") String email);

}
