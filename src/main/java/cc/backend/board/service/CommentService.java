package cc.backend.board.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.dto.request.CommentRequest;
import cc.backend.board.dto.response.CommentCreateResponse;
import cc.backend.board.dto.response.CommentResponse;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.entity.CommentLike;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentLikeRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.PostEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher eventPublisher; //이벤트 테스트
    //댓글 작성
    @Transactional
    public CommentCreateResponse createComment(Long boardId, CommentRequest req) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        Member member = memberRepository.findById(req.getMemberId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Comment comment;
        if (req.getParentCommentId() == null) {
            // 댓글
            comment = Comment.createComment(req.getContent(), member, board);
        } else {
            // 대댓글
            Comment parent = commentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
            if (parent.getDepth() != 0) {
                throw new GeneralException(ErrorStatus.COMMENT_DEPTH_EXCEEDED);
            }
            comment = Comment.createReply(req.getContent(), member, board, parent);
        }
        commentRepository.save(comment);

        eventPublisher.publishEvent(new CommentEvent(comment.getBoard().getId(), comment.getMember().getId()));   //이벤트 생성

        board.increaseCommentCount();

        Long boardWriterId = board.getMember().getId();
        return CommentCreateResponse.from(comment, boardWriterId);
    }

    //댓글/대댓글 수정
    @Transactional
    public CommentCreateResponse updateComment(Long memberId, Long commentId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        if (!comment.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.COMMENT_ACCESS_DENIED);
        }
        comment.updateContent(newContent);

        // 게시글 작성자 id 추출
        Long boardWriterId = comment.getBoard().getMember().getId();
        return CommentCreateResponse.from(comment, boardWriterId);
    }

    // 댓글/대댓글 삭제
    @Transactional
    public void deleteComment(Long memberId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        if (!comment.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.COMMENT_ACCESS_DENIED);
        }
        commentRepository.delete(comment); // soft delete

        Board board = comment.getBoard();
        board.decreaseCommentCount();
    }

    // 댓글/대댓글 목록 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        List<Comment> comments = commentRepository.findByBoardOrderByCreatedAtAsc(board);
        Long boardWriterId = board.getMember().getId();

        //  모든 댓글을 Map<id, CommentResponse>로 변환
        Map<Long, CommentResponse> map = new LinkedHashMap<>();
        for (Comment comment : comments) {
            map.put(comment.getId(), CommentResponse.from(comment, boardWriterId));
        }

        // 트리 구조로 변환
        List<CommentResponse> result = new ArrayList<>();
        for (Comment comment : comments) {
            CommentResponse response = map.get(comment.getId());
            if (comment.getParent() == null) {
                // 최상위 댓글
                result.add(response);
            } else {
                // 대댓글을 부모의 children에 추가
                CommentResponse parentResponse = map.get(comment.getParent().getId());
                parentResponse.getChildren().add(response);
            }
        }
        return result;
    }

    //댓글 좋아요
    @Transactional
    public int toggleCommentLike(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Optional<CommentLike> existingLike = commentLikeRepository.findByMemberAndComment(member, comment);

        if (existingLike.isPresent()) {
            // 이미 좋아요한 경우: 취소
            commentLikeRepository.delete(existingLike.get());
            comment.decreaseLikeCount();
            return -1;
        } else {
            // 좋아요 추가
            CommentLike like = CommentLike.of(member, comment);
            commentLikeRepository.save(like);
            comment.increaseLikeCount();
            return 1;
        }
    }


}
