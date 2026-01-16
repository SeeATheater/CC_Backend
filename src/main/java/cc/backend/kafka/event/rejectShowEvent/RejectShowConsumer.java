package cc.backend.kafka.event.rejectShowEvent;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.notice.service.NoticeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RejectShowConsumer {

    private final NoticeService noticeService;

    @KafkaListener(
            topics = "reject-show-topic",
            groupId = "reject-notice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(RejectShowEvent event) {
        if (event == null) return;

        noticeService.notifyRejection(event);
    }
}
