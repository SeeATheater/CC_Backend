package cc.backend.event.service;

import cc.backend.event.entity.PostEvent;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostEventListener {
    private final NoticeService noticeService;

    @EventListener
    public NoticeResponseDTO.NoticeDTO handlePostCreate(PostEvent event) {

        // 알림 전송 등 로직
        return noticeService.notifyNewBoard(event);
    }
}
