package cc.backend.kafka.event.replyEvent;

import cc.backend.kafka.repository.ProcessedEventRepository;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cc.backend.kafka.event.common.DomainEventValidator.requireNonNullEvent;

@Component
@RequiredArgsConstructor
public class ReplyConsumer {

    private static final String CONSUMER_GROUP = "reply-notice-group";

    private final ProcessedEventRepository processedEventRepository;
    private final NoticeService noticeService;

    @KafkaListener(
            topics = "reply-created-topic",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(ReplyEvent event) {
        requireNonNullEvent(event);

        int inserted = processedEventRepository.insertIfAbsent(
                event.eventId(),
                CONSUMER_GROUP
        );

        if (inserted == 0) {
            return;
        }

        noticeService.notifyNewReply(event);
    }
}
