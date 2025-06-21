package cc.backend.notice.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.repository.BoardRepository;
import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.PostEvent;
import cc.backend.event.entity.PromoteHotEvent;
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

import javax.xml.stream.events.Comment;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {
    private final BoardRepository boardRepository;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final MemberNoticeRepository memberNoticeRepository;
   // private final CommentRepository commentRepository;

    @Transactional
    @Override
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(PromoteHotEvent event) {
        Long boardId = event.getBoardId();

        Member writer = memberRepository.findById(event.getWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String content = "GET board/" + boardId + " HTTP/1.1";

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                .type(NoticeType.BOARD)
                .title("회원님의 게시글이 Hot 게시물로 선정되었습니다.")
                .content(content)
                .build()
        );

        memberNoticeRepository.save(
                MemberNotice.builder()
                        .member(writer)
                        .notice(newNotice)
                        .build()
        );

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .message("회원님의 게시글이 Hot 게시물로 선정되었습니다.")
                .noticeType(NoticeType.BOARD)
                .count(1L)
                .createdAt(newNotice.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyCommentBoard(CommentEvent event) {
        Long boardId = event.getBoardId();

        Member writer = memberRepository.findById(event.getWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String content = "GET board/" + boardId + "HTTP/1.1";   //board로 redirect 되는 url

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.BOARD)
                        .title("회원님의 게시글에 댓글이 달렸습니다.")
                        .content(content)
                        .build()
        );

         memberNoticeRepository.save(
                MemberNotice.builder()
                .member(writer)
                .notice(newNotice)
                .build()
        );

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .message("회원님의 게시글에 댓글이 달렸습니다.")
                .noticeType(NoticeType.BOARD)
                .count(1L)
                .createdAt(newNotice.getCreatedAt())
                .build();

    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyNewBoard(PostEvent event){
        Long boardId = event.getBoardId();

        List<Member> receivers = event.getMembers();

        String content = "GET board/" + boardId + "HTTP/1.1";   //board로 redirect 되는 url

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.BOARD)
                        .title(event.getWriterId() + "게시글이 올라왔습니다.")
                        .content(content)
                        .build()
        );

        memberNoticeRepository.saveAll(
                receivers.stream()
                        .map(member -> MemberNotice.builder()
                                .notice(newNotice)
                                .member(member)
                                .build())
                        .collect(Collectors.toList())
        );

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .message( "memberId" + event.getWriterId() + "가 올린 게시글 알림")
                .noticeType(NoticeType.BOARD)
                .count((long) receivers.size())
                .createdAt(newNotice.getCreatedAt())
                .build();

    }

//    @Override
//    @Transactional
//    public NoticeResponseDTO.BoardNoticeResultDTO notifyReplyBoard(Long commentId) {
//        Comment comment = commentRepository.findById(boardId)
//                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
//
//        Long memberId = board.getMember().getId();
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
//
//        String content = "urlLinkToComment";
//
//        Notice notice = Notice.builder()
//                .type(NoticeType.BOARD)
//                .title("회원님의 댓글에 대댓글이 달렸습니다.")
//                .content(content)
//                .member(member)
//                .build();
//
//        Notice newNotice = noticeRepository.save(notice);
//        return NoticeResponseDTO.BoardNoticeResultDTO.builder()
//                .id(newNotice.getId())
//                .noticeType(newNotice.getType())
//                .title(newNotice.getTitle())
//                .content(newNotice.getContent())
//                .build();
//
//    }


}
