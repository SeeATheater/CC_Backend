package cc.backend.photoAlbum.service;

import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PhotoAlbumService {

    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId);
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO getPhotoAlbum(Long photoAlbumId, Long memberId);
    public List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO> getPhotoAlbumList(Long memberId);

}
