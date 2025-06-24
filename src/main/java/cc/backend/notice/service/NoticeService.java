package cc.backend.notice.service;

import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.NewShowEvent;
import cc.backend.event.entity.PromoteHotEvent;
import cc.backend.event.entity.ReplyEvent;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.dto.NoticeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(PromoteHotEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewComment(CommentEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewReply(ReplyEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewShow(NewShowEvent event);

}
