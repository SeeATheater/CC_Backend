package cc.backend.image.repository;

import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByFilePathAndContentId(FilePath filePath, Long contentId);
}
