package cc.backend.board.service;

import cc.backend.board.dto.request.BoardRequest;
import cc.backend.board.dto.response.BoardDetailResponse;
import cc.backend.board.dto.response.BoardResponse;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardLike;
import cc.backend.board.entity.HotBoard;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.board.repository.BoardLikeRepository;
import cc.backend.board.repository.HotBoardRepository;
import cc.backend.member.entity.Member;
import cc.backend.board.repository.BoardRepository;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;
    private final HotBoardRepository hotBoardRepository;
    private final MemberRepository memberRepository;

    // 게시글 작성
    //TODO : 이미지 업로드 코드 추가
    @Transactional
    public BoardResponse createBoard(Long memberId, BoardRequest dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 홍보게시판은 Performer만 작성 가능
        if (dto.getBoardType() == BoardType.PROMOTION && member.getRole() != Role.PERFORMER) {
            throw new IllegalArgumentException("홍보게시판은 Performer만 작성할 수 있습니다.");
        }

        Board board = Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imgUrls(dto.getImgUrls())
                .boardType(dto.getBoardType())
                .likeCount(0)
                .commentCount(0)
                .commentMaxIndex(0)
                .member(member)
                .build();

        boardRepository.save(board);
        return BoardResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(board.getImgUrls())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }

    //게시글 수정
    @Transactional
   public  BoardResponse updateBoard( Long memberId, Long boardId, BoardRequest dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 작성자 본인만 수정 가능
        if (!board.getMember().getId().equals(memberId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        board.update(dto.getTitle(), dto.getContent(), dto.getImgUrls(), dto.getBoardType());

        return BoardResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(board.getImgUrls())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();

    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long memberId, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 작성자 본인만 삭제 가능
        if (!board.getMember().getId().equals(memberId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        boardRepository.delete(board); //soft delete
    }

    //게시글 조회
    @Transactional(readOnly = true)
    public Slice<BoardDetailResponse> getBoards(BoardType boardType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Slice<Board> boardSlice = boardRepository.findAllByBoardTypeOrderByIdDesc(boardType, pageable);
        return boardSlice.map(BoardDetailResponse::from);
    }

    //게시글 상세 조회
    @Transactional(readOnly = true)
    public BoardDetailResponse getBoard(Long boardId) {
        Board board=boardRepository.findById(boardId)
                .orElseThrow(()-> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        return BoardDetailResponse.from(board);
    }

    //게시글 좋아요
    @Transactional
    public int toggleLike(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Optional<BoardLike> existingLike = boardLikeRepository.findByMemberAndBoard(member, board);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            boardLikeRepository.delete(existingLike.get());
            board.decreaseLikeCount();
            return -1;
        } else {
            // 좋아요 추가
            BoardLike newLike = BoardLike.of(member, board);
            boardLikeRepository.save(newLike);
            board.increaseLikeCount();
            promoteToHotBoard(board);
            return 1;
        }
    }

    //핫게시판 조회
    @Transactional(readOnly = true)
    public List<BoardDetailResponse> getHotBoards() {
        List<HotBoard> hotBoards = hotBoardRepository.findTop10ByOrderByBoard_CreatedAtDesc();
        // 등록일 순으로 정렬
        return hotBoards.stream()
                .sorted(Comparator.comparing(hb -> hb.getBoard().getCreatedAt()))
                .map(hb -> BoardDetailResponse.from(hb.getBoard()))
                .collect(Collectors.toList());
    }

    // --------------- 내부 메서드 ------------
    //핫게시판 선정 로직
    private void promoteToHotBoard(Board board) {
        // 이미 핫게시글이면 아무것도 하지 않음
        if (hotBoardRepository.findByBoard(board).isPresent()) {
            return;
        }
        if (board.getLikeCount() >= 10) {
            // 핫게시글이 10개 이상이면 가장 오래된 것 제거
            if (hotBoardRepository.count() >= 10) {
                List<HotBoard> hotBoards = hotBoardRepository.findTop10ByOrderByHotRegisteredAtAsc();
                HotBoard oldest = hotBoards.get(0);
                hotBoardRepository.delete(oldest);
            }
            // 핫게시글로 등록
            HotBoard hotBoard = HotBoard.builder()
                    .board(board)
                    .hotRegisteredAt(LocalDateTime.now())
                    .build();
            hotBoardRepository.save(hotBoard);
        }
    }



}