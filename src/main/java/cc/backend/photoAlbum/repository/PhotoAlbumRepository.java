package cc.backend.photoAlbum.repository;

import cc.backend.photoAlbum.entity.PhotoAlbum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    List<PhotoAlbum> searchPhotoAlbumByKeyword(@Param("keyword") String keyword);

    @Query("SELECT pa FROM PhotoAlbum pa " +
            "JOIN pa.amateurShow a " +
            "WHERE a.member.id = :performerId " +
            "AND (:cursorId IS NULL OR pa.id < :cursorId) " +
            "ORDER BY pa.updatedAt DESC")
    List<PhotoAlbum> findByPerformerWithCursor(@Param("performerId") Long performerId, @Param("cursorId") Long cursorId, Pageable pageable
    );

    @Query("SELECT p FROM PhotoAlbum p WHERE (:cursorId IS NULL OR p.id < :cursorId) ORDER BY p.updatedAt DESC")
    List<PhotoAlbum> findNextAlbums(@Param("cursorId") Long cursorId, Pageable pageable);
}
