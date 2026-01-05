package cc.backend.amateurShow.repository.specification;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.member.entity.Member;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AmateurShowSpecification {

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

    /**
     * 오늘 날짜 회차 필터
     * performanceDateTime이 전달된 날짜(date)와 일치하는 공연만
     */
    public static Specification<AmateurShow> hasRoundOn(LocalDate date) {
        return (root, query, cb) -> {
            // 중복 제거 필요 시
            query.distinct(true);

            // Join to amateurRounds
            var rounds = root.join("amateurRounds");

            // 날짜만 비교 (MySQL 기준)
            return cb.equal(cb.function("DATE", LocalDate.class, rounds.get("performanceDateTime")), date);
        };
    }

    /**
     * 마지막 회차 날짜가 오늘인 공연 필터
     */
    public static Specification<AmateurShow> hasLastRoundOn(LocalDate today) {
        return (root, query, cb) -> {
            query.distinct(true);

            // 서브쿼리: AmateurRounds 기준
            var subquery = query.subquery(LocalDate.class);
            var roundRoot = subquery.from(AmateurRounds.class);

            // MAX(DATE(performanceDateTime))
            Expression<LocalDate> lastRoundDate =
                    cb.greatest(
                            cb.function(
                                    "DATE",
                                    LocalDate.class,
                                    roundRoot.get("performanceDateTime")
                            )
                    );

            subquery.select(lastRoundDate);

            // 해당 공연의 회차만 대상
            subquery.where(
                    cb.equal(roundRoot.get("amateurShow"), root)
            );

            // 마지막 회차 날짜 == today
            return cb.equal(subquery, today);
        };
    }
}
