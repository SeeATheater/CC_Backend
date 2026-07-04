package cc.backend.kafka.event.hotBoardEvent;

import cc.backend.kafka.repository.ProcessedEventRepository;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static cc.backend.kafka.event.common.DomainEventValidator.requireNonNullEvent;

@Component
@RequiredArgsConstructor
public class HotBoardConsumer {

    private static final String CONSUMER_GROUP = "hot-board-notice-group";

    private final ProcessedEventRepository processedEventRepository;
    private final NoticeService noticeService;

    @KafkaListener(
            topics = "hot-board-topic",
            groupId = CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(HotBoardEvent event) {
        requireNonNullEvent(event);

        int inserted = processedEventRepository.insertIfAbsent(
                event.eventId(),
                CONSUMER_GROUP
        );

        if (inserted == 0) {
            return;
        }

        noticeService.notifyHotBoard(event);
    }
}
