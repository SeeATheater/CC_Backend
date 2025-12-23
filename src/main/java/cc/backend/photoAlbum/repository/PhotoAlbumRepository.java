package cc.backend.photoAlbum.repository;

import cc.backend.photoAlbum.entity.PhotoAlbum;
import org.springframework.data.domain.Page;
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

    /**
     * Retrieves photo albums for the performer identified by the given id.
     *
     * @param performerId the id of the performer whose photo albums to retrieve
     * @param pageable pagination and sorting information for the result page
     * @return a Page of PhotoAlbum entities belonging to shows whose member.id equals {@code performerId}
     */
    @Query("SELECT pa FROM PhotoAlbum pa " +
            "JOIN pa.amateurShow a " +
            "WHERE a.member.id = :performerId "
    )
    Page<PhotoAlbum> findByPerformer(
            @Param("performerId") Long performerId,
            Pageable pageable
    );

    /**
     * Retrieve the next set of PhotoAlbum entities located before the given cursor, ordered by `updatedAt` descending.
     *
     * @param cursorId  an exclusive upper bound on album `id`; when `null` the query returns from the newest albums
     * @param pageable  pagination and sorting constraints to apply to the result set
     * @return a list of PhotoAlbum objects that match the cursor constraint, ordered by `updatedAt` descending
     */
    @Query("SELECT p FROM PhotoAlbum p WHERE (:cursorId IS NULL OR p.id < :cursorId) ORDER BY p.updatedAt DESC")
    List<PhotoAlbum> findNextAlbums(@Param("cursorId") Long cursorId, Pageable pageable);
}