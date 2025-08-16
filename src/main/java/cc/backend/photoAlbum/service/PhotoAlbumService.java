package cc.backend.photoAlbum.service;

import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PhotoAlbumService {

    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO getPhotoAlbum(Long photoAlbumId, Long memberId);
    public PhotoAlbumResponseDTO.PerformerPhotoAlbumDTO getPhotoAlbumList(Long memberId, Long performerID);
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO updatePhotoAlbum(Long photoAlbumId, PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    public String deletePhotoAlbum(Long photoAlbumId, Long memberId);
    public List<PhotoAlbumResponseDTO.MemberPhotoAlbumDTO> getAllPhotoAlbumList();
}
