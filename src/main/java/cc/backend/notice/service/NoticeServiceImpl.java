package cc.backend.notice.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.AmateurTicketRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.event.entity.*;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {
    private final BoardRepository boardRepository;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final MemberNoticeRepository memberNoticeRepository;
    private final AmateurShowRepository amateurShowRepository;
    private final CommentRepository commentRepository;
    private final AmateurTicketRepository amateurTicketRepository;

    @Transactional
    @Override
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(PromoteHotEvent event) {
        Long boardId = event.getBoardId();

        Member writer = memberRepository.findById(event.getWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                .type(NoticeType.HOT)
                .message("회원님의 게시글이 HOT 게시글에 등록되었습니다!")
                .contentId(boardId)
                .build()
        );

        memberNoticeRepository.save(
                MemberNotice.builder()
                        .member(writer)
                        .notice(newNotice)
                        .build());

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .message(newNotice.getMessage())
                .noticeType(newNotice.getType())
                .contentId(boardId)
                .createdAt(newNotice.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyNewComment(CommentEvent event) {
        Board board = boardRepository.findById(event.getBoardId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        Member boardWriter = memberRepository.findById(event.getBoardWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Comment comment = commentRepository.findById(event.getCommentId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        Member commentWriter = memberRepository.findById(event.getCommentWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if(boardWriter.equals(commentWriter)) {
            return null;
        }

        String preview = comment.getContent().length() > 15 ? comment.getContent().substring(0, 15) + "..." : comment.getContent();

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.COMMENT)
                        .message("게시글 '" + board.getTitle() + "'에 새로운 댓글이 달렸습니다.\n" + '"' + preview + '"')
                        .contentId(event.getBoardId())
                        .build()
        );

        memberNoticeRepository.save(MemberNotice.builder()
                                 .notice(newNotice)
                                 .member(commentWriter).build());

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .message(newNotice.getMessage())
                .noticeType(newNotice.getType())
                .contentId(newNotice.getContentId())
                .createdAt(newNotice.getCreatedAt())
                .build();

    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyNewShow(NewShowEvent event){
        Long amateurShowId = event.getAmateurShowId();

        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        List<Member> receivers = event.getMembers();

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.AMATEURSHOW)
                        .message("소극장 공연 " + '"' + amateurShow.getName() + '"' + " 등록 완료! 소극장 공연 페이지에서 확인해보세요!")
                        .contentId(amateurShowId)
                        .build()
        );

        memberNoticeRepository.saveAll(
                receivers.stream()
                        .map(member -> MemberNotice.builder()
                                .notice(newNotice)
                                .member(member)
                                .build())
                        .collect(Collectors.toList()));

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .message(newNotice.getMessage())
                .noticeType(newNotice.getType())
                .contentId(newNotice.getContentId())
                .createdAt(newNotice.getCreatedAt())
                .build();

    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyNewReply(ReplyEvent event) {


        Comment comment = commentRepository.findById(event.getCommentId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        Comment reply = commentRepository.findById(event.getReplyId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        Member commentWriter = memberRepository.findById(event.getCommentWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Member replyWriter = memberRepository.findById(event.getReplyWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));


        if(commentWriter.equals(replyWriter)){
            return null;
        }

        String commentPreview = comment.getContent().length() > 15 ? comment.getContent().substring(0, 15) + "..." : comment.getContent();
        String replyPreview = reply.getContent().length() > 15 ? reply.getContent().substring(0, 15) + "..." : reply.getContent();

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                    .type(NoticeType.REPLY)
                    .message("댓글 " + '"' + commentPreview + '"' +  "에 새로운 대댓글이 달렸습니다.\n" + '"' + replyPreview + '"')
                    .contentId(event.getCommentId())
                    .build()
        );

        memberNoticeRepository.save(
                        MemberNotice.builder()
                                .notice(newNotice)
                                .member(commentWriter)
                                .build());

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .noticeType(newNotice.getType())
                .message(newNotice.getMessage())
                .contentId(newNotice.getContentId())
                .createdAt(newNotice.getCreatedAt())
                .build();

    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyTicketReservation(TicketReservationEvent event) {

        Notice notice = noticeRepository.save(Notice.builder()
                .type(NoticeType.TICKET)
                .message("'" + event.getAmateurShow().getName() + '"' + " 공연 예약이 완료되었습니다.")
                .contentId(event.getAmateurTicket().getId())
                .build()
        );

        memberNoticeRepository.save(MemberNotice.builder()
                .notice(notice)
                .member(event.getMember())
                .build()
        );

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .noticeType(notice.getType())
                .message(notice.getMessage())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();
    }


}
