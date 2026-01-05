package cc.backend.photoAlbum.service;

import cc.backend.photoAlbum.dto.PerformerShowListResponseDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PhotoAlbumService {

    public PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    public PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO getPhotoAlbum(Long photoAlbumId, Long memberId);
    public Slice<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO> getPhotoAlbumList(Long memberId, Long performerId, Pageable pageable);
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO updatePhotoAlbum(Long photoAlbumId, PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    public String deletePhotoAlbum(Long photoAlbumId, Long memberId);
    public PhotoAlbumResponseDTO.ScrollMemberPhotoAlbumDTO getAllRecentPhotoAlbumList(Long cursorId, int size);
    public PerformerShowListResponseDTO getPerformerShows(Long memberId, Pageable pageable);
    public List<PhotoAlbumResponseDTO.MyShowsForPhotoAlbumDTO> getMyShows(Long memberId);
}
