package cc.backend.notice.service;

import cc.backend.event.entity.*;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.dto.NoticeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(PromoteHotEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewComment(CommentEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewReply(ReplyEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewShow(NewShowEvent event);
    public NoticeResponseDTO.NoticeDTO notifyTicketReservation(TicketReservationEvent event);
    public NoticeResponseDTO.NoticeDTO notifyApproval(ApproveShowEvent event);
    public NoticeResponseDTO.NoticeDTO notifyRejection(RejectShowEvent event);

}
