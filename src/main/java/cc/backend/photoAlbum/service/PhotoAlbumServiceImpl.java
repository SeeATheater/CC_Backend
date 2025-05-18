package cc.backend.photoAlbum.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.repository.PhotoAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoAlbumServiceImpl implements PhotoAlbumService {

    private final AmateurShowRepository amateurShowRepository;
    private final PhotoAlbumRepository photoAlbumRepository;

    @Override
    @Transactional
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO){

        Long amateurShowId = requestDTO.getAmateurShowId();
        String title = requestDTO.getTitle();
        String content = requestDTO.getContent();
        List<String> imageUrls = requestDTO.getImageUrls();
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        PhotoAlbum newPhotoAlbum = PhotoAlbum.builder()
                .title(title)
                .content(content)
                .imageUrls(imageUrls)
                .amateurShow(amateurShow)
                .build();

        PhotoAlbum photoAlbum = photoAlbumRepository.save(newPhotoAlbum);
        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(photoAlbum.getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .title(photoAlbum.getTitle())
                .content(photoAlbum.getContent())
                .place(photoAlbum.getAmateurShow().getPlace())
                .schedule(photoAlbum.getAmateurShow().getSchedule())
                .imageUrls(photoAlbum.getImageUrls())
                .build();
    }

    @Override
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO getPhotoAlbum(Long photoAlbumId){
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(photoAlbum.getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .title(photoAlbum.getTitle())
                .content(photoAlbum.getContent())
                .place(photoAlbum.getAmateurShow().getPlace())
                .schedule(photoAlbum.getAmateurShow().getSchedule())
                .imageUrls(photoAlbum.getImageUrls())
                .build();
    }

    @Override
    public PhotoAlbumResponseDTO.PhotoAlbumListDTO getPhotoAlbumList(Long memberId){ //로그인 구현 시 member로 받도록 수정
        List<AmateurShow> amateurShows = amateurShowRepository.findAllByMemberId(memberId);
        List<Long> amateurShowIds = amateurShows.stream().map(AmateurShow::getId).collect(Collectors.toList());
        List<PhotoAlbum> photoAlbums = amateurShowIds.stream()
                .flatMap(id -> photoAlbumRepository.findAllByAmateurShowIdOrderByCreatedAt(id).stream())
                .collect(Collectors.toList());
        List<PhotoAlbumResponseDTO.PhotoAlbumResultDTO> photoAlbumDTOs = photoAlbums.stream()
                .map(photoAlbum -> PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                        .photoAlbumId(photoAlbum.getId())  // 앨범 ID 설정
                        .amateurShowName(photoAlbum.getAmateurShow().getName())  // 아마추어 쇼 이름
                        .title(photoAlbum.getTitle())  // 앨범 제목
                        .content(photoAlbum.getContent())  // 앨범 내용
                        .place(photoAlbum.getAmateurShow().getPlace())  // 공연 장소
                        .schedule(photoAlbum.getAmateurShow().getSchedule())  // 공연 일정
                        .imageUrls(photoAlbum.getImageUrls())  // 이미지 URL 리스트
                        .build())
                .collect(Collectors.toList());

        return PhotoAlbumResponseDTO.PhotoAlbumListDTO.builder()
                .photoAlbumDTOs(photoAlbumDTOs)
                .firstImageUrl(photoAlbumDTOs.get(0).getImageUrls().get(0))
                .total(photoAlbumDTOs.size())
                .build();

    }
}
