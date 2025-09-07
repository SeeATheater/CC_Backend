package cc.backend.event.service;

import cc.backend.event.entity.ApproveShowEvent;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApproveShowEventListener {
    private final NoticeService noticeService;

    @EventListener
    public NoticeResponseDTO.NoticeDTO handleApproveShowEvent(ApproveShowEvent event) {

        return noticeService.notifyApproval(event);
    }

}
