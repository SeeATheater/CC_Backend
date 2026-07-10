package cc.backend.event.service;

import cc.backend.event.entity.ApproveShowEvent;
import cc.backend.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApproveShowEventListener {
    private final NoticeService noticeService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApproveShowEvent(ApproveShowEvent event) {
        executeSafely("approval", event, () -> noticeService.notifyApproval(event));
        executeSafely("likers", event, () -> noticeService.notifyLikers(event));
        executeSafely("recommendation", event, () -> noticeService.notifyRecommendation(event));
    }

    private void executeSafely(String notificationType, ApproveShowEvent event, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error(
                    "공연 승인 알림 처리 실패: type={}, showId={}, memberId={}",
                    notificationType,
                    event.getAmateurShowId(),
                    event.getMemberId(),
                    e
            );
        }
    }
}
