package cc.backend.amateurShow.repository.specification;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AmateurShowSpecification {

    public static Specification<AmateurShow> isAudienceVisible() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("approvalStatus"), ApprovalStatus.APPROVED);
    }

    public static Specification<AmateurShow> isWrittenBy(Member performer) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("member"), performer);
    }

    /**
     * Audience(일반 사용자)가 볼 수 있는 공연 (승인 상태가 'APPROVED')
     */
    public static Specification<AmateurShow> isApproved() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("approvalStatus"), ApprovalStatus.APPROVED);
    }

    /**
     * 현재 진행 중인 공연 (오늘 날짜가 시작일과 종료일 사이)
     */
    public static Specification<AmateurShow> isOngoing(LocalDate today) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("start"), today),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("end"), today)
                );
    }

    /**
     * 아직 종료되지 않은 공연 (종료일이 오늘이거나 그 이후)
     */
    public static Specification<AmateurShow> isNotEnded(LocalDate today) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("end"), today);
    }
}
