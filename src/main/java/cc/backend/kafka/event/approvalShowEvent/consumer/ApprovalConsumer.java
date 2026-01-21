package cc.backend.kafka.event.approvalShowEvent.consumer;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.notice.service.NoticeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalConsumer {

    private final NoticeService noticeService;

    @KafkaListener(
            topics = "approval-show-topic",
            groupId = "approved-performer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )

    @Transactional
    public void consume(ApprovalShowEvent event) {
        if (event == null) return;

        noticeService.notifyApproval(event);
    }
}
