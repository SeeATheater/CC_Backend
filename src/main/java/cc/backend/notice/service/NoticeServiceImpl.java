package cc.backend.notice.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.NewShowEvent;
import cc.backend.event.entity.PromoteHotEvent;
import cc.backend.event.entity.ReplyEvent;
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

    @Transactional
    @Override
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(PromoteHotEvent event) {
        Long boardId = event.getBoardId();

        Member writer = memberRepository.findById(event.getWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                .type(NoticeType.BOARD)
                .message("회원님의 게시글이 HOT 게시글에 등록되었습니다.")
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
        Long boardId = event.getBoardId();

        Member writer = memberRepository.findById(event.getWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.BOARD)
                        .message("게시글" + board.getTitle() + "에 새로운 댓글이 달렸습니다.")
                        .contentId(boardId)
                        .build()
        );


        memberNoticeRepository.save(MemberNotice.builder()
                                 .notice(newNotice)
                                 .member(writer).build());

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
                        .message("소극장 공연" + amateurShow.getName()+ "등록 완료! 소극장 공연 페이지에서 확인해보세요!")
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

        Long commentId = event.getCommentId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));

        Long writerId = event.getWriterId();
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Notice newNotice = noticeRepository.save(
                Notice.builder()
                    .type(NoticeType.BOARD)
                    .message("댓글" + comment.getContent() + "에 새로운 대댓글이 달렸습니다.")
                    .contentId(commentId)
                    .build()
        );

        memberNoticeRepository.save(
                        MemberNotice.builder()
                                .notice(newNotice)
                                .member(writer)
                                .build());

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(newNotice.getId())
                .noticeType(newNotice.getType())
                .message(newNotice.getMessage())
                .contentId(newNotice.getContentId())
                .createdAt(newNotice.getCreatedAt())
                .build();

    }


}
