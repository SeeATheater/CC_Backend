package cc.backend.notice.service;

import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.kafka.event.rejectShowEvent.RejectShowProducer;
import cc.backend.notice.event.RejectCommitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class RejectCommitEventListener {

    private final RejectShowProducer rejectShowProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRejectCommit(RejectCommitEvent event) {

        // REJECTED 수정 트랜잭션 커밋 완료 후 Kafka 이벤트 발송
        rejectShowProducer.publish(
                new RejectShowEvent(event.amateurShowId(), event.performerId(), event.rejectReason())
        );
    }
}
