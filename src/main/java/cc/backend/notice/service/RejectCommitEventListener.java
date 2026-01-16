package cc.backend.notice.service;

import cc.backend.kafka.event.approvalShowEvent.ApprovalShowProducer;
import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.kafka.event.rejectShowEvent.RejectShowProducer;
import cc.backend.notice.event.RejectCommitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RejectCommitEventListener {
    private final RejectShowProducer rejectShowProducer;

    // REJECTED 수정 트랜잭션 커밋 완료 후 Kafka 이벤트 발송
    public void onRejectCommit(RejectCommitEvent event) {
        rejectShowProducer.publish(
                new RejectShowEvent(event.amateurShowId(), event.performerId(), event.rejectReason())
        );
    }
}
