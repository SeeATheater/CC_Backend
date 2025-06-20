package cc.backend.notice.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.repository.BoardRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.events.Comment;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {
    private final BoardRepository boardRepository;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
   // private final CommentRepository commentRepository;

    @Transactional
    @Override
    public NoticeResponseDTO.BoardNoticeResultDTO notifyHotBoard(Long boardId){

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        Long memberId = board.getMember().getId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String content = "urlLinkToBoard";

        Notice notice = Notice.builder()
                .type(NoticeType.BOARD)
                .title("회원님의 게시글이 Hot 게시물로 선정되었습니다.")
                .content(content)
                .member(member)
                .build();

        Notice newNotice = noticeRepository.save(notice);
        return NoticeResponseDTO.BoardNoticeResultDTO.builder()
                .id(newNotice.getId())
                .noticeType(newNotice.getType())
                .title(newNotice.getTitle())
                .content(newNotice.getContent())
                .build();
    }

    @Override
    @Transactional
    public NoticeResponseDTO.BoardNoticeResultDTO notifyCommentBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        Long memberId = board.getMember().getId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String content = "urlLinkToComment";

        Notice notice = Notice.builder()
                .type(NoticeType.BOARD)
                .title("회원님의 게시글에 댓글이 달렸습니다.")
                .content(content)
                .member(member)
                .build();

        Notice newNotice = noticeRepository.save(notice);
        return NoticeResponseDTO.BoardNoticeResultDTO.builder()
                .id(newNotice.getId())
                .noticeType(newNotice.getType())
                .title(newNotice.getTitle())
                .content(newNotice.getContent())
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
