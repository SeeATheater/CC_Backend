package cc.backend.notice.service.eventListener;

import cc.backend.kafka.event.commentEvent.CommentEvent;
import cc.backend.kafka.event.commentEvent.CommentProducer;
import cc.backend.notice.event.CommentCommitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class CommentCommitEventListener {
    private final CommentProducer commentProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCommit(CommentCommitEvent event) {

        // Comment 생성 트랜잭션 커밋 완료 후 Kafka 이벤트 발송
        commentProducer.publish(
               new CommentEvent(event.boardId(), event.boardWriterId(), event.commentId(), event.commentWriterId())
       );
    }
}
