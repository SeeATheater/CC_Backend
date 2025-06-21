package cc.backend.notice.service;

import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.PostEvent;
import cc.backend.event.entity.PromoteHotEvent;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.dto.NoticeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(PromoteHotEvent event);
    public NoticeResponseDTO.NoticeDTO notifyCommentBoard(CommentEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewBoard(PostEvent event);
}
