package cc.backend.notice.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.projection.PerformerHashtagView;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.hotBoardEvent.HotBoardEvent;
import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.kafka.event.reservationCompletedEvent.ReservationCompletedEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.kafka.event.commentEvent.CommentEvent;
import cc.backend.kafka.event.replyEvent.ReplyEvent;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final MemberLikeRepository memberLikeRepository;

    @Transactional
    @Override
    public NoticeResponseDTO.NoticeDTO notifyHotBoard(HotBoardEvent event) {
        Long boardId = event.boardId();

        Member writer = memberRepository.findById(event.writerId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Notice notice = noticeRepository.save(
                Notice.builder()
                .type(NoticeType.HOT)
                .message("회원님의 게시글이 HOT 게시글에 등록되었습니다!")
                .contentId(boardId)
                .build()
        );

        memberNoticeRepository.save(
                MemberNotice.builder()
                        .member(writer)
                        .notice(notice)
                        .build());

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .message(notice.getMessage())
                .noticeType(notice.getType())
                .contentId(boardId)
                .createdAt(notice.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyNewComment(CommentEvent event) {
        // 1. 게시글 조회
        Board board = boardRepository.findById(event.boardId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        // 2. 게시글 작성자 조회
        Member boardWriter = memberRepository.findById(event.boardWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 3. 댓글 조회
        Comment comment = commentRepository.findById(event.commentId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));

        // 4. 댓글 작성자 조회
        Member commentWriter = memberRepository.findById(event.commentWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 5. 자기 글에 자기가 댓글 단 경우 알림 X
        if (boardWriter.equals(commentWriter)) {
            return null;
        }

        // 6. 댓글 미리보기 생성
        String preview = comment.getContent().length() > 15
                ? comment.getContent().substring(0, 15) + "..."
                : comment.getContent();

        // 7. Notice 생성
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.COMMENT)
                        .message(
                                "게시글 '" + board.getTitle() + "'에 새로운 댓글이 달렸습니다.\n"
                                        + "\"" + preview + "\""
                        )
                        .contentId(board.getId())
                        .build()
        );

        // 8. 게시글 작성자에게 알림 발송
        memberNoticeRepository.save(
                MemberNotice.builder()
                        .notice(notice)
                        .member(boardWriter)
                        .build()
        );

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .message(notice.getMessage())
                .noticeType(notice.getType())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();

    }


    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyRejection(RejectShowEvent event) {
        // 1. 공연 조회
        AmateurShow show = amateurShowRepository.findById(event.amateurShowId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        // 2. 공연자 조회
        Member performer = memberRepository.findById(event.performerId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 3. 반려 알림 생성
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.AMATEURSHOW)
                        .message(
                                "요청하신 '" + show.getName() + "' 공연 등록이 반려되었습니다.\n"
                                        + event.rejectReason()
                        )
                        .contentId(show.getId())
                        .build()
        );

        // 4. 공연자에게 알림 발송
        memberNoticeRepository.save(
                MemberNotice.builder()
                        .notice(notice)
                        .member(performer)
                        .build()
        );

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .message(notice.getMessage())
                .noticeType(notice.getType())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyNewReply(ReplyEvent event) {

        Comment comment = commentRepository.findById(event.commentId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        Comment reply = commentRepository.findById(event.replyId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        Member commentWriter = memberRepository.findById(event.commentWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Member replyWriter = memberRepository.findById(event.replyWriterId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if(commentWriter.equals(replyWriter)){
            return null;
        }

        String commentPreview = comment.getContent().length() > 15
                ? comment.getContent().substring(0, 15) + "..."
                : comment.getContent();

        String replyPreview = reply.getContent().length() > 15
                ? reply.getContent().substring(0, 15) + "..."
                : reply.getContent();

        Notice notice = noticeRepository.save(
                Notice.builder()
                    .type(NoticeType.REPLY)
                    .message("댓글 " + '"' + commentPreview + '"' +  "에 새로운 대댓글이 달렸습니다.\n" + '"' + replyPreview + '"')
                    .contentId(event.commentId())
                    .build()
        );

        memberNoticeRepository.save(
                        MemberNotice.builder()
                                .notice(notice)
                                .member(commentWriter)
                                .build());

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .noticeType(notice.getType())
                .message(notice.getMessage())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();

    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyTicketReservation(ReservationCompletedEvent event) {
        AmateurShow amateurShow = amateurShowRepository.findById(event.amateurShowId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        Member member = memberRepository.findById(event.memberId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));


        Notice notice = noticeRepository.save(Notice.builder()
                .type(NoticeType.TICKET)
                .message("'" + amateurShow.getName() + "'" + " 공연 예약이 완료되었습니다.")
                .contentId(event.realTicketId())
                .build()
        );

        memberNoticeRepository.save(MemberNotice.builder()
                .notice(notice)
                .member(member)
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

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyRecommendation(ApprovalShowEvent event) {

        if (noticeRepository.existsByContentIdAndType(
                event.amateurShowId(), NoticeType.RECOMMEND)) {
            return null;
        }
        // 1. 공연 조회
        AmateurShow show = amateurShowRepository.findById(event.amateurShowId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));


        // 2. 새 공연 해시태그 계산
        Set<String> newTagsSet = parseHashtags(show.getHashtag());

        if (newTagsSet.isEmpty()) return null; // 태그 없으면 추천 불가


        // 3. 추천 대상 회원을 일괄 조회한 데이터로 계산
        List<Member> targets = findRecommendationTargets(show, newTagsSet);
        if (targets.isEmpty()) return null;

        // 4. Notice 생성
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.RECOMMEND)
                        .message("새로운 공연 '" + show.getName() + "' 어떠세요?")
                        .contentId(show.getId())
                        .build()
        );

        // 5. 개인화 알림 생성
        List<MemberNotice> memberNotices = targets.stream()
                .map(member -> MemberNotice.builder()
                        .member(member)
                        .notice(notice)
                        .personalMsg(createPersonalMessage(show, member))
                        .isRead(false)
                        .build())
                .toList();

        memberNoticeRepository.saveAll(memberNotices);

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .noticeType(notice.getType())
                .message(notice.getMessage())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();

    }

    private Set<String> parseHashtags(String raw) {
        return Arrays.stream(Optional.ofNullable(raw).orElse("").split("[#,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private List<Member> findRecommendationTargets(
            AmateurShow newShow,
            Set<String> newTags
    ) {
        // 1. 좋아요 테이블 전체를 읽기 전에 승인된 과거 공연의 태그로
        //    신규 공연과 취향이 겹치는 공연자 ID부터 좁힌다.
        Set<Long> matchedPerformerIds = amateurShowRepository
                .findApprovedHistoricalPerformerHashtags(
                        ApprovalStatus.APPROVED,
                        newShow.getId()
                )
                .stream()
                .filter(row -> !Collections.disjoint(
                        newTags,
                        parseHashtags(row.getHashtag())
                ))
                .map(PerformerHashtagView::getPerformerId)
                .collect(Collectors.toSet());

        if (matchedPerformerIds.isEmpty()) return List.of();

        // 2. 관련 공연자를 좋아한 회원만 DB에서 조회한다.
        //    DISTINCT로 여러 공연자를 좋아한 동일 회원의 중복 알림을 방지한다.
        return memberLikeRepository.findDistinctLikersByPerformerIdIn(
                matchedPerformerIds
        );
    }

    private String createPersonalMessage(AmateurShow show, Member member) {
        return "새로운 공연 '" + show.getName() + "' 어떠세요? "
                + show.getHashtag() + " "
                + member.getName() + "님 취향에 딱!";
    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyApproval(ApprovalShowEvent event) {
        AmateurShow show = amateurShowRepository.findById(event.amateurShowId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        Member performer = memberRepository.findById(event.performerId())
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 승인된 공연 알림 생성
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.AMATEURSHOW)
                        .message("요청하신 '" + show.getName() + "' 공연 등록이 승인되었습니다.")
                        .contentId(event.amateurShowId())
                        .build()
        );

        // 공연 등록자에게 MemberNotice 발송
        MemberNotice memberNotice = MemberNotice.builder()
                .notice(notice)
                .member(performer)
                .build();

        memberNoticeRepository.save(memberNotice);

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .noticeType(notice.getType())
                .message(notice.getMessage())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();

    }

    @Override
    @Transactional
    public NoticeResponseDTO.NoticeDTO notifyLikers(ApprovalShowEvent event) {
        Long amateurShowId = event.amateurShowId();

        // 공연 조회
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        // 공연자 좋아요한 유저 조회
        List<MemberLike> likers = memberLikeRepository.findByPerformerId(event.performerId());
        if (likers.isEmpty()) return null;

        // 등록 알림 생성
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.AMATEURSHOW)
                        .message("소극장 공연 '" + amateurShow.getName() + "' 등록 완료! 소극장 공연 페이지에서 확인해보세요!")
                        .contentId(amateurShowId)
                        .build()
        );

        // 등록자 계정 좋아요한 사람에게 MemberNotice 발송
        List<MemberNotice> memberNotices = likers.stream()
                .map(liker -> MemberNotice.builder()
                        .notice(notice)
                        .member(liker.getLiker())
                        .build())
                .toList();

        memberNoticeRepository.saveAll(memberNotices);

        return NoticeResponseDTO.NoticeDTO.builder()
                .id(notice.getId())
                .noticeType(notice.getType())
                .message(notice.getMessage())
                .contentId(notice.getContentId())
                .createdAt(notice.getCreatedAt())
                .build();
    }


}
