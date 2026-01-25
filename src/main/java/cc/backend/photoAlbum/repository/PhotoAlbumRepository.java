package cc.backend.photoAlbum.repository;

import cc.backend.photoAlbum.entity.PhotoAlbum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Long> {
    @Query("""
        SELECT p FROM PhotoAlbum p
        WHERE CAST(p.id AS string) LIKE %:keyword%
           OR CAST(p.amateurShow.id AS string) LIKE %:keyword%
           OR p.amateurShow.name LIKE %:keyword%
           OR p.content LIKE %:keyword%
    """)
    Page<PhotoAlbum> searchPhotoAlbumByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT pa FROM PhotoAlbum pa
    JOIN pa.amateurShow a
    JOIN a.member m
    WHERE m.id = :performerId
""")
    Slice<PhotoAlbum> findByPerformerId(
            @Param("performerId") Long performerId,
            Pageable pageable
    );


    @Query("""
    SELECT p
    FROM PhotoAlbum p
    LEFT JOIN p.amateurShow a
    WHERE ((:cursorId IS NULL OR :cursorUpdatedAt IS NULL)
           OR p.updatedAt < :cursorUpdatedAt
           OR (p.updatedAt = :cursorUpdatedAt AND p.id < :cursorId))
    ORDER BY p.updatedAt DESC, p.id DESC
    """)
    List<PhotoAlbum> findNextAlbums(@Param("cursorId") Long cursorId, @Param("cursorUpdatedAt") LocalDateTime cursorUpdatedAt, Pageable pageable);
}
