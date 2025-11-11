package cc.backend.member.repository;

import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Member> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    boolean existsByUsername(String username);


    Optional<Member> findByRole(Role role);

    boolean existsByRole(Role role);
}
