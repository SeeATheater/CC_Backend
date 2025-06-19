package cc.backend.board.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.dto.response.BoardReportResponse;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardReport;
import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.repository.BoardReportRepository;
import cc.backend.board.repository.BoardRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardReportService {

    private final BoardReportRepository boardReportRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    // 신고 누적 5회시 -> delete
    private static final int REPORT_THRESHOLD = 5;

    @Transactional
    public BoardReportResponse reportBoard(Long memberId, Long boardId, ReportReason reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        // 중복 신고 방지
        if (boardReportRepository.existsByMemberAndBoard(member, board)) {
            throw new GeneralException(ErrorStatus.ALREADY_REPORTED);
        }

        BoardReport report = BoardReport.builder()
                .member(member)
                .board(board)
                .reason(reason)
                .build();
        boardReportRepository.save(report);

        // 누적 신고 처리
        long reportCount = boardReportRepository.countByBoard(board);
        if (reportCount >= REPORT_THRESHOLD && !board.isDeleted()) {
            boardRepository.delete(board); // soft delete
        }

        return BoardReportResponse.builder()
                .boardId(boardId)
                .memberId(memberId)
                .reason(reason)
                .reasonDescription(reason.getDescription())
                .build();
    }
}
