package cc.backend.kafka.event.commentEvent;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CommentConsumer {

    private final NoticeService noticeService;

    @KafkaListener(
            topics = "comment-created-topic",
            groupId = "comment-notice-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(CommentEvent event) {

        if (event == null) return;

        noticeService.notifyNewComment(event);

    }
}
