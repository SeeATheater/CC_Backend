package cc.backend.notice.service;

import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.hotBoardEvent.HotBoardEvent;
import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.kafka.event.reservationCompletedEvent.ReservationCompletedEvent;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.kafka.event.commentEvent.CommentEvent;
import cc.backend.kafka.event.replyEvent.ReplyEvent;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(HotBoardEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewComment(CommentEvent event);
    public NoticeResponseDTO.NoticeDTO notifyNewReply(ReplyEvent event);
    public NoticeResponseDTO.NoticeDTO notifyTicketReservation(ReservationCompletedEvent event);
    public NoticeResponseDTO.NoticeDTO notifyRejection(RejectShowEvent event);
    public NoticeResponseDTO.NoticeDTO notifyRecommendation(ApprovalShowEvent event);
    public NoticeResponseDTO.NoticeDTO notifyApproval(ApprovalShowEvent event);
    public NoticeResponseDTO.NoticeDTO notifyLikers(ApprovalShowEvent event);

}
