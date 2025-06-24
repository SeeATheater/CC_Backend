package cc.backend.event.service;

import cc.backend.event.entity.PromoteHotEvent;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.hibernate.procedure.ProcedureOutputs;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromoteHotEventListner {
    private final NoticeService noticeService;

    @EventListener
    public NoticeResponseDTO.NoticeDTO handleHotPromote(PromoteHotEvent event) {

        // 알림 전송 등 로직
        return noticeService.notifyHotBoard(event);
    }
}
