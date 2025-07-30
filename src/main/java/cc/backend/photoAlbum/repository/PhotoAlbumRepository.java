package cc.backend.photoAlbum.repository;

import cc.backend.photoAlbum.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Long> {
    List<PhotoAlbum> findAllByAmateurShowId(Long amateurShowId);
    List<PhotoAlbum> findAllByOrderByUpdatedAtDesc();
    @Query("""
        SELECT p FROM PhotoAlbum p
        WHERE CAST(p.id AS string) LIKE %:keyword%
           OR CAST(p.amateurShow.id AS string) LIKE %:keyword%
           OR p.amateurShow.name LIKE %:keyword%
           OR p.content LIKE %:keyword%
    """)
    List<PhotoAlbum> searchPhotoAlbumByKeyword(@Param("keyword") String keyword);
}
