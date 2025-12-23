package cc.backend.photoAlbum.service;

import cc.backend.photoAlbum.dto.PerformerShowListResponseDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PhotoAlbumService {

    /**
 * Creates a new photo album for the specified member.
 *
 * @param requestDTO the data required to create the photo album (title, description, media metadata, etc.)
 * @param memberId   the ID of the member who will own the new album
 * @return           a DTO representing the created photo album, including presigned upload URLs for the album's media
 */
public PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    /**
 * Retrieve a photo album by its ID as viewed by the specified member.
 *
 * The returned object includes album metadata and presigned URLs for accessing the album's stored assets.
 *
 * @param photoAlbumId the ID of the photo album to retrieve
 * @param memberId the requesting member's ID used to determine access and personalization
 * @return a PhotoAlbumResultWithPresignedUrlDTO containing the album data and presigned URLs for its assets
 */
public PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO getPhotoAlbum(Long photoAlbumId, Long memberId);
    /**
 * Retrieves a paginated list of a performer's photo albums as visible to a specific member.
 *
 * @param memberId    the ID of the member requesting the list (used to determine visibility and personalization)
 * @param performerId the ID of the performer whose photo albums are being requested
 * @param page        zero-based page index to return
 * @param size        the maximum number of albums per page
 * @return            a DTO containing the requested page of the performer's photo albums and related pagination metadata
 */
public PhotoAlbumResponseDTO.PerformerPhotoAlbumDTO getPhotoAlbumList(Long memberId, Long performerId, int page, int size);
    /**
 * Updates an existing photo album identified by photoAlbumId with the provided data for the specified member.
 *
 * @param photoAlbumId ID of the photo album to update
 * @param requestDTO DTO containing the updated photo album fields
 * @param memberId ID of the member performing the update
 * @return the updated photo album representation
 */
public PhotoAlbumResponseDTO.PhotoAlbumResultDTO updatePhotoAlbum(Long photoAlbumId, PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    /**
 * Deletes the specified photo album for the given member.
 *
 * @param photoAlbumId the ID of the photo album to delete
 * @param memberId the ID of the member requesting the deletion
 * @return a string message describing the result of the deletion operation (for example, a success or error message)
 */
public String deletePhotoAlbum(Long photoAlbumId, Long memberId);
    /**
 * Retrieves a paginated list of recent photo albums.
 *
 * @param page the page index to retrieve
 * @param size the number of items per page
 * @return a DTO containing the requested page of recent member photo albums and pagination metadata
 */
public PhotoAlbumResponseDTO.ScrollMemberPhotoAlbumDTO getAllRecentPhotoAlbumList(int page, int size);
    /**
 * Retrieve a pageable list of performer shows associated with a member.
 *
 * @param memberId the ID of the requesting member used to personalize or filter results
 * @param pageable pagination and sorting information for the result set
 * @return a PerformerShowListResponseDTO containing performer show entries and pagination metadata
 */
public PerformerShowListResponseDTO getPerformerShows(Long memberId, Pageable pageable);
}