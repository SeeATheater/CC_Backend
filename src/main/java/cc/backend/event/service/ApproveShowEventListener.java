package cc.backend.event.service;

import cc.backend.event.entity.ApproveShowEvent;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApproveShowEventListener {
    private final NoticeService noticeService;

    @EventListener
    public void handleApproveShowEvent(ApproveShowEvent event) {
        // ApplicationEventPublisher의 동기 호출 흐름 안에서 승인 이벤트 하나를
        // 등록자, 좋아요 회원, 취향 추천 알림으로 순차 fan-out 한다.
        noticeService.notifyApproval(event);
        noticeService.notifyLikers(event);
        noticeService.notifyRecommendation(event);
    }

}
