package cc.backend.notice.unit;

import cc.backend.event.entity.ApproveShowEvent;
import cc.backend.event.service.ApproveShowEventListener;
import cc.backend.notice.service.NoticeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;

/**
 * 승인 Spring Event가 동기 Listener 안에서 세 알림 로직으로 순차 fan-out되는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ApproveShowEventListenerTest {

    @Mock
    private NoticeService noticeService;

    @Mock
    private ApproveShowEvent event;

    @InjectMocks
    private ApproveShowEventListener listener;

    @Test
    void handlesApprovalLikerAndRecommendationNotificationsSynchronously() {
        listener.handleApproveShowEvent(event);

        InOrder inOrder = inOrder(noticeService);
        inOrder.verify(noticeService).notifyApproval(event);
        inOrder.verify(noticeService).notifyLikers(event);
        inOrder.verify(noticeService).notifyRecommendation(event);
    }
}
