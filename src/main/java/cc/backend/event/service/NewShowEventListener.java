package cc.backend.event.service;

import cc.backend.event.entity.NewShowEvent;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewShowEventListener {
    private final NoticeService noticeService;

    @EventListener
    public NoticeResponseDTO.NoticeDTO handleAmateurShowCreate(NewShowEvent event) {

        // 알림 전송 등 로직
        return noticeService.notifyNewShow(event);
    }
}
