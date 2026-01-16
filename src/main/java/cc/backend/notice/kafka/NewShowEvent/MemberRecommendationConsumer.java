package cc.backend.notice.kafka.NewShowEvent;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemberRecommendationConsumer {
    private final NoticeRepository noticeRepository;
    private final MemberNoticeRepository memberNoticeRepository;
    private final MemberRepository memberRepository;

    @KafkaListener(
            topics = "member-recommendation-event",
            groupId = "recommendation-group",
            containerFactory = "kafkaListenerContainerFactory"   // batch 수신
    )
    @Transactional
    public void consume(List<MemberRecommendationEvent> events) { // batch 소비
        if (events == null || events.isEmpty()) return;

        // 이벤트에서 MemberId, NoticeId 수집
        Set<Long> memberIds = events.stream()
                .map(MemberRecommendationEvent::getMemberId)
                .collect(Collectors.toSet());

        Set<Long> noticeIds = events.stream()
                .map(MemberRecommendationEvent::getNoticeId)
                .collect(Collectors.toSet());

        // DB에서 한 번에 조회 후 Map 생성
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds)
                .stream().collect(Collectors.toMap(Member::getId, m -> m));

        Map<Long, Notice> noticeMap = noticeRepository.findAllById(noticeIds)
                .stream().collect(Collectors.toMap(Notice::getId, n -> n));

        // MemberNotice 생성
        List<MemberNotice> memberNotices = new ArrayList<>();

        for (MemberRecommendationEvent event : events) {
            Member member = memberMap.get(event.getMemberId());
            Notice notice = noticeMap.get(event.getNoticeId());

            if (member == null || notice == null) {
                // 일부 이벤트 실패 시 로깅 후 건너뜀
                // (원하면 DLQ로 보내거나 재처리 가능)
                System.err.println("Member or Notice not found for event: " + event);
                continue;
            }

            memberNotices.add(MemberNotice.builder()
                    .member(member)
                    .notice(notice)
                    .personalMsg(event.getMessage())
                    .isRead(false)
                    .build());
        }

        if (!memberNotices.isEmpty()) {
            memberNoticeRepository.saveAll(memberNotices); // batch insert
        }
    }
}
