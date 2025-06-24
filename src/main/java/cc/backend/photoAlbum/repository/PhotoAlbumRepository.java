package cc.backend.photoAlbum.repository;

import cc.backend.photoAlbum.entity.PhotoAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Long> {
    List<PhotoAlbum> findAllByAmateurShowId(Long amateurShowId);
}
