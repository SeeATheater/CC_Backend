package cc.backend.admin.board.service;

import cc.backend.admin.board.dto.response.AdminBoardDetailResponse;
import cc.backend.admin.board.dto.response.AdminBoardDetailWithCommentsResponse;
import cc.backend.admin.board.dto.response.AdminBoardListResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.dto.response.CommentResponse;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.Comment;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.board.service.CommentService;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminBoardService {

    private final BoardRepository boardRepository;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;

    //게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<AdminBoardListResponse> getAllBoardsForAdmin(int page, int size, String keyword) {
        // 관리자는 삭제된 게시글도 볼 수 있도록 @SQLDelete 우회
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Board> boards = (keyword != null && !keyword.isBlank())
                ? boardRepository.searchBoardsIncludingDeletedByTitle(keyword, pageable)
                : boardRepository.findAllBoardsIncludingDeleted(pageable);

        List<AdminBoardListResponse> content = boards.getContent().stream()
                .map(AdminBoardListResponse::from)
                .toList();
        return new PageImpl<>(content, pageable, boards.getTotalElements());
    }

    //게시글 상세조회
    @Transactional(readOnly = true)
    public AdminBoardDetailWithCommentsResponse getBoardDetailForAdmin(Long boardId) {
        // 게시글 조회 (삭제된 것도 포함)
        Board board = boardRepository.findByIdIncludingDeleted(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));


        // 상세 조회에서는 모든 이미지 조회
        List<String> imgUrls = imageRepository.findAllByFilePathAndContentId(FilePath.board, boardId)
                .stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        AdminBoardDetailResponse boardDetail = AdminBoardDetailResponse.of(board, imgUrls);


        // 댓글 목록 조회
        List<CommentResponse> comments = new ArrayList<>();
        if (!board.isDeleted()) {
            // 삭제되지 않은 게시글의 경우에만 댓글 조회
            comments = commentService.getCommentsForAdmin(boardId);
        }

        return AdminBoardDetailWithCommentsResponse.builder()
                .boardDetail(boardDetail)
                .comments(comments)
                .build();
    }


    // 게시글/댓글 삭제
    @Transactional
    public void deleteReportedTarget(ReportTarget targetType, Long targetId) {
        if (targetType == ReportTarget.BOARD) {
            Board board = boardRepository.findById(targetId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
            boardRepository.delete(board); // soft delete
        } else if (targetType == ReportTarget.COMMENT) {
            Comment comment = commentRepository.findById(targetId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
            commentRepository.delete(comment); // soft delete

            Board board = comment.getBoard();
            board.decreaseCommentCount();
        }
    }

}
