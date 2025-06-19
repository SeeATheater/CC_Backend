package cc.backend.admin.board.service;

import cc.backend.admin.board.dto.response.ReportedBoardSummary;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardReport;
import cc.backend.board.repository.BoardReportRepository;
import cc.backend.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBoardService {
    private final BoardReportRepository boardReportRepository;
    private final BoardRepository boardRepository;

    /**
     * 신고된 게시글 조회
     */
    @Transactional(readOnly = true)
    public List<ReportedBoardSummary> getReportedBoards() {
        List<Board> boards = boardReportRepository.findAllReportedBoards();
        List<ReportedBoardSummary> result = new ArrayList<>();
        for (Board board : boards) {
            List<BoardReport> reports = board.getReports();
            ReportedBoardSummary summary = ReportedBoardSummary.builder()
                    .boardId(board.getId())
                    .title(board.getTitle())
                    .writerId(board.getMember().getId())
                    .reportCount((long) reports.size())
                    .deleted(board.isDeleted())
                    .reports(
                            reports.stream()
                                    .map(r -> ReportedBoardSummary.ReportDetail.builder()
                                            .reportId(r.getId())
                                            .reporterId(r.getMember().getId())
                                            .reason(r.getReason().getDescription())
                                            .reportedAt(r.getCreatedAt())
                                            .build())
                                    .collect(Collectors.toList())
                    )
                    .build();
            result.add(summary);
        }
        return result;
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void adminDeleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        boardRepository.delete(board); // soft delete
    }


}
