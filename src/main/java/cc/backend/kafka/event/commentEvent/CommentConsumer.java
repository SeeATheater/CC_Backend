package cc.backend.kafka.event.commentEvent;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.kafka.repository.ProcessedEventRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cc.backend.kafka.event.common.DomainEventValidator.requireNonNullEvent;

@Component
@RequiredArgsConstructor
public class CommentConsumer {

    private static final String CONSUMER_GROUP = "comment-notice-group";

    private final ProcessedEventRepository processedEventRepository;
    private final NoticeService noticeService;

    @KafkaListener(
            topics = "comment-created-topic",
            groupId = "comment-notice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(CommentEvent event) {
        requireNonNullEvent(event);

        int inserted = processedEventRepository.insertIfAbsent(
                event.eventId(),
                CONSUMER_GROUP
        );

        if (inserted == 0) {
            return; // 이미 처리한 이벤트 -> 무시
        }

        noticeService.notifyNewComment(event);

    }
}
