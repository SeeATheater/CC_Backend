package cc.backend.notice.service;

import cc.backend.kafka.event.replyEvent.ReplyEvent;
import cc.backend.kafka.event.replyEvent.ReplyProducer;
import cc.backend.notice.event.ReplyCommitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ReplyCommmitEventListener {

    private final ReplyProducer replyProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReplyCommit(ReplyCommitEvent event) {

        // Reply 생성 트랜잭션 커밋 완료 후 Kafka 이벤트 발송
        replyProducer.publish( new ReplyEvent(
                event.commentId(),
                event.commentWriterId(),
                event.replyId(),
                event.replyWriterId()
                )
        );
    }
}
