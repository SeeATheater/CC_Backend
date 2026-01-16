package cc.backend.kafka.event.replyEvent;

import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplyConsumer {

    private final NoticeService noticeService;

    @KafkaListener(
            topics = "reply-created-topic",
            groupId = "reply-notice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ReplyEvent event) {
        if (event == null) return;

        noticeService.notifyNewReply(event);

    }
}
