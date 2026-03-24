package cc.backend.notice.service.eventListener;

import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowProducer;
import cc.backend.notice.event.ApproveCommitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ApproveCommitEventListener {
    private final ApprovalShowProducer approvalShowProducer;

    @Async
    @TransactionalEventListener (phase = TransactionPhase.AFTER_COMMIT)
    public void onApproveCommit(ApproveCommitEvent event) {

        //APPROVED 수정 트랜잭션 커밋 이벤트 감지 후 kafka 이벤트 발송
        approvalShowProducer.publish(
                new ApprovalShowEvent(event.amateurShowId(), event.performerId())
        );
    }
}
