package cc.backend.board.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.dto.response.ReportResponse;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.entity.Report;
import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.board.repository.ReportRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    private static final int BOARD_REPORT_THRESHOLD = 5;
    private static final int COMMENT_REPORT_THRESHOLD = 5;

    @Transactional
    public ReportResponse report(Long reporterId, ReportTarget targetType, Long targetId, ReportReason reason) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 신고 대상 존재 여부 체크
        if (targetType == ReportTarget.BOARD) {
            boardRepository.findById(targetId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        } else if (targetType == ReportTarget.COMMENT) {
            commentRepository.findById(targetId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
        }

        // 중복 신고 방지
        if (reportRepository.existsByReporterAndTargetTypeAndTargetId(reporter, targetType, targetId)) {
            throw new GeneralException(ErrorStatus.ALREADY_REPORTED);
        }

        // 신고 저장
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .reason(reason)
                .build();
        reportRepository.save(report);

        // 누적 신고 처리 (게시글/댓글 각각)
        long reportCount = reportRepository.countByTargetTypeAndTargetId(targetType, targetId);

        if (targetType == ReportTarget.BOARD && reportCount >= BOARD_REPORT_THRESHOLD) {
            Board board = boardRepository.findById(targetId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
            if (!board.isDeleted()) boardRepository.delete(board);
        }
        if (targetType == ReportTarget.COMMENT && reportCount >= COMMENT_REPORT_THRESHOLD) {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
            if (!comment.isDeleted()) commentRepository.delete(comment);
        }

        return ReportResponse.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporterId(reporterId)
                .reason(reason)
                .reasonDescription(reason.getDescription())
                .build();
    }
}

