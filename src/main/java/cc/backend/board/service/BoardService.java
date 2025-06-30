package cc.backend.board.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.dto.request.BoardRequest;
import cc.backend.board.dto.request.BoardSearchRequest;
import cc.backend.board.dto.response.BoardDetailResponse;
import cc.backend.board.dto.response.BoardResponse;
import cc.backend.board.entity.Board;
import cc.backend.board.entity.BoardLike;
import cc.backend.board.entity.HotBoard;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.board.repository.BoardLikeRepository;
import cc.backend.board.repository.HotBoardRepository;
import cc.backend.event.entity.PromoteHotEvent;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.service.ImageService;
import cc.backend.member.entity.Member;
import cc.backend.board.repository.BoardRepository;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ImageService imageService;

    private final ApplicationEventPublisher eventPublisher;

    // 게시글 작성
    @Transactional
    public BoardResponse createBoard(Long memberId, BoardRequest dto) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->  new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 홍보게시판은 Performer만 작성 가능
        if (dto.getBoardType() == BoardType.PROMOTION && member.getRole() != Role.PERFORMER) {
            throw new GeneralException(ErrorStatus.ONLY_PERFORMER_CAN_WRITE_PROMOTION);
        }

        //contentId 얻기 위해 먼저 게시글 저장
        Board board = Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .boardType(dto.getBoardType())
                .likeCount(0)
                .commentCount(0)
                .member(member)
                .build();

        Board savedBoard = boardRepository.save(board);

        //이미지 저장
        List<String> imgUrls = new ArrayList<>();
        if (dto.getImageRequestDTOs() != null && !dto.getImageRequestDTOs().isEmpty()) {
            List<ImageRequestDTO.FullImageRequestDTO> fullImageRequestDTOs = dto.getImageRequestDTOs()
                    .stream()
                    .map(imageDto -> ImageRequestDTO.FullImageRequestDTO.builder()
                            .keyName(imageDto.getKeyName())
                            .imageUrl(imageDto.getImageUrl())
                            .filePath(FilePath.board) // FilePath enum 사용
                            .contentId(savedBoard.getId()) // 저장된 게시글 ID 사용
                            .memberId(memberId)
                            .build())
                    .collect(Collectors.toList());

            List<ImageResponseDTO.ImageResultDTO> savedImages = imageService.saveImages(fullImageRequestDTOs);
            imgUrls = savedImages.stream()
                    .map(ImageResponseDTO.ImageResultDTO::getImageUrl)
                    .collect(Collectors.toList());
        }

        return BoardResponse.builder()
                .boardId(savedBoard.getId())
                .boardType(savedBoard.getBoardType())
                .title(savedBoard.getTitle())
                .content(savedBoard.getContent())
                .imgUrls(imgUrls) // 응답에 포함
                .createdAt(savedBoard.getCreatedAt())
                .updatedAt(savedBoard.getUpdatedAt())
                .build();
    }

    //게시글 수정
    @Transactional
   public  BoardResponse updateBoard( Long memberId, Long boardId, BoardRequest dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!board.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.BOARD_ACCESS_DENIED);
        }

        board.update(dto.getTitle(), dto.getContent(), dto.getBoardType());

        return BoardResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(board.getImages().stream()
                        .map(Image::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();

    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long memberId, Long boardId) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));

        // 작성자 본인만 삭제 가능
        if (!board.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.BOARD_ACCESS_DENIED);
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
                .orElseThrow(()-> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        return BoardDetailResponse.from(board);
    }

    //게시글 좋아요
    @Transactional
    public int toggleLike(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOARD_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

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

    //TODO : 햔재는 Type에 따라 검색 (추후 통합 검색 변경될 수 있음)
    //게시판 검색
    @Transactional(readOnly = true)
    public Slice<BoardDetailResponse> searchBoards(BoardSearchRequest request) {
        if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            // 검색어가 없으면 일반 조회
            return getBoards(request.getBoardType(), request.getPage(), request.getSize());
        }

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").descending());
        Slice<Board> boardSlice;

        if (request.getBoardType() == BoardType.NORMAL) {
            // 일반게시판: 제목 + 내용 검색
            boardSlice = boardRepository.searchNormalBoards(
                    request.getBoardType(), request.getKeyword(), pageable);
        } else if (request.getBoardType() == BoardType.PROMOTION) {
            // 홍보게시판: 제목 + 내용 + 작성자 검색
            boardSlice = boardRepository.searchPromotionBoards(
                    request.getBoardType(), request.getKeyword(), pageable);
        } else {
            throw new GeneralException(ErrorStatus.INVALID_BOARD_TYPE);
        }

        return boardSlice.map(BoardDetailResponse::from);
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

            eventPublisher.publishEvent(new PromoteHotEvent(board.getId(), board.getMember().getId())); //핫게 이벤트 생성
        }
    }



}