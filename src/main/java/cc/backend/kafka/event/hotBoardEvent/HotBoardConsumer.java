package cc.backend.kafka.event.hotBoardEvent;

import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class HotBoardConsumer {

    private final NoticeService noticeService;

    @KafkaListener(
            topics = "hot-board-topic",
            groupId = "hot-board-notice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(HotBoardEvent event) {
        if (event == null) return;

        noticeService.notifyHotBoard(event);
    }
}
