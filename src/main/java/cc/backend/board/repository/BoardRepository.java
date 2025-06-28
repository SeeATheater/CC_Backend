package cc.backend.board.repository;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.enums.BoardType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    Slice<Board> findAllByBoardTypeOrderByIdDesc(BoardType boardType, Pageable pageable);

    @Query(value = "SELECT * FROM board WHERE id = :id", nativeQuery = true)
    Optional<Board> findByIdIncludingDeleted(@Param("id") Long id);
    // 일반게시판: 제목 + 내용 검색
    @Query("SELECT b FROM Board b WHERE b.boardType = :boardType AND b.deleted = false AND (b.title LIKE %:keyword% OR b.content LIKE %:keyword%)")
    Slice<Board> searchNormalBoards(
            @Param("boardType") BoardType boardType,
            @Param("keyword") String keyword,
            Pageable pageable);

    // 홍보게시판: 제목 + 내용 + 작성자 검색
    @Query("SELECT b FROM Board b WHERE b.boardType = :boardType AND b.deleted = false AND (b.title LIKE %:keyword% OR b.content LIKE %:keyword% OR b.member.username LIKE %:keyword%)")
    Slice<Board> searchPromotionBoards(
            @Param("boardType") BoardType boardType,
            @Param("keyword") String keyword,
            Pageable pageable);
}

