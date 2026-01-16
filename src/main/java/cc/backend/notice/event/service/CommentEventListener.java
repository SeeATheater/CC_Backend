package cc.backend.notice.event.service;

import cc.backend.notice.event.entity.CommentEvent;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentEventListener {
    private final NoticeService noticeService;

    @EventListener
    public NoticeResponseDTO.NoticeDTO handleCommentCreate(CommentEvent event) {

        return noticeService.notifyNewComment(event);
    }
}
