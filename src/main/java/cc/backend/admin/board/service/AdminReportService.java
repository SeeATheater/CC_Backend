package cc.backend.admin.board.service;

import cc.backend.admin.board.dto.response.AdminReportSummary;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.entity.Report;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.board.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    /**
     * 신고된 게시글/댓글 조회
     */
    @Transactional(readOnly = true)
    public List<AdminReportSummary> getReports() {
        // 모든 신고를 조회
        List<Report> reports = reportRepository.findAll();

        // 신고를 targetType, targetId로 그룹핑
        Map<String, List<Report>> grouped = reports.stream()
                .collect(Collectors.groupingBy(r -> r.getTargetType() + "_" + r.getTargetId()));

        //그룹별로 AdminReportSummary 생성
        List<AdminReportSummary> result = new ArrayList<>();
        for (List<Report> group : grouped.values()) {
            Report any = group.get(0); // 그룹 내 아무거나(모두 같은 대상)
            ReportTarget targetType = any.getTargetType();
            Long targetId = any.getTargetId();

            String targetContent;
            Long writerId;
            boolean deleted;


            if (targetType == ReportTarget.BOARD) {
                Board board = boardRepository.findByIdIncludingDeleted(targetId).orElse(null);
                if (board == null) {
                    targetContent = "[데이터 없음]";
                    writerId = null;
                    deleted = false;
                } else {
                    targetContent = board.getTitle();
                    writerId = (board.getMember() != null) ? board.getMember().getId() : null;
                    deleted = board.isDeleted();
                }
            } else {
                Comment comment = commentRepository.findByIdIncludingDeleted(targetId).orElse(null);
                if (comment == null) {
                    targetContent = "[데이터 없음]";
                    writerId = null;
                    deleted = false;
                } else {
                    targetContent = comment.getContent();
                    writerId = (comment.getMember() != null) ? comment.getMember().getId() : null;
                    deleted = comment.isDeleted();
                }
            }

            List<AdminReportSummary.ReportDetail> reportDetails = group.stream()
                    .map(r -> AdminReportSummary.ReportDetail.builder()
                            .reportId(r.getId())
                            .reporterId(r.getReporter().getId())
                            .reason(r.getReason().name())
                            .reportedAt(r.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            AdminReportSummary summary = AdminReportSummary.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetContent(targetContent)
                    .writerId(writerId)
                    .reportCount(reportDetails.size())
                    .deleted(deleted)
                    .reports(reportDetails)
                    .build();

            result.add(summary);
        }
        return result;
    }

}
