package cc.backend.inquiry.repository;

import cc.backend.inquiry.entity.Inquiry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    Slice<Inquiry> findByMemberId(Long memberId, Pageable pageable);


}
