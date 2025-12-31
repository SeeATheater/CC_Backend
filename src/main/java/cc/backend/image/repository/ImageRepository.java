package cc.backend.image.repository;

import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByFilePathAndContentId(FilePath filePath, Long contentId);
    Image findByFilePathAndContentId(FilePath filePath, Long contentId);
    @Query("SELECT i FROM Image i " +
            "WHERE i.contentId IN :contentIds " +
            "AND i.filePath = :filePath " +
            "AND i.id = (SELECT MIN(i2.id) FROM Image i2 WHERE i2.contentId = i.contentId)")
    List<Image> findFirstByContentIds(
            @Param("contentIds") List<Long> contentIds,
            @Param("filePath") FilePath filePath
    );
    <Optional> Image findByKeyName(String keyName);

    <Optional> Image findByFilePathAndKeyName(FilePath filePath, String keyName);

    List<Image> findByFilePathAndContentIdIn(
            FilePath filePath,
            List<Long> contentIds
    );
}
