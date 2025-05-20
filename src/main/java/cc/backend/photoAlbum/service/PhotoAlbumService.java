package cc.backend.photoAlbum.service;

import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface PhotoAlbumService {

    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO);
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO getPhotoAlbum(Long photoAlbumId);
    public PhotoAlbumResponseDTO.PhotoAlbumListDTO getPhotoAlbumList(Long memberId);
}
