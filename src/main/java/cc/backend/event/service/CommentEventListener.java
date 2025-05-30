package cc.backend.event.service;

import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.PostEvent;
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
    public NoticeResponseDTO.BoardNoticeResultDTO handleCommentCreate(CommentEvent event) {

        System.out.println("댓글 달림!  = " + event.getId());// 알림 전송 등 로직
        return noticeService.notifyCommentBoard(event.getId());
    }
}
