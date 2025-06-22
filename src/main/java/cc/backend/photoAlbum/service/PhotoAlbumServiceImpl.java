package cc.backend.photoAlbum.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import cc.backend.image.service.ImageService;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.repository.PhotoAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoAlbumServiceImpl implements PhotoAlbumService {

    private final AmateurShowRepository amateurShowRepository;
    private final PhotoAlbumRepository photoAlbumRepository;
    private final ImageRepository imageRepository;
    private final ImageService imageService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Long amateurShowId = requestDTO.getAmateurShowId();
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        PhotoAlbum newPhotoAlbum = PhotoAlbum.builder()
                .amateurShow(amateurShow)
                .content(requestDTO.getContent())
                .build();

        PhotoAlbum photoAlbum = photoAlbumRepository.save(newPhotoAlbum);

        List<ImageRequestDTO.FullImageRequestDTO> fullImageRequestDTOs = requestDTO.getImageRequestDTOs()
                .stream()
                .map(dto-> ImageRequestDTO.FullImageRequestDTO.builder()
                        .keyName(dto.getKeyName())
                        .imageUrl(dto.getImageUrl())
                        .filePath(FilePath.photoAlbum)
                        .contentId(photoAlbum.getId())
                        .memberId(member.getId())
                        .build()).toList();

        List<ImageResponseDTO.ImageResultDTO> imageResultDTOs = imageService.saveImages(fullImageRequestDTOs);

        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(photoAlbum.getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .content(photoAlbum.getContent())
                .place(photoAlbum.getAmateurShow().getPlace())
                .schedule(photoAlbum.getAmateurShow().getSchedule())
                .imageResultDTOs(imageResultDTOs)
                .build();
    }

    @Override
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO getPhotoAlbum(Long photoAlbumId, Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbumId);

        List<ImageResponseDTO.ImageResultDTO> imageResultDTOs = images.stream()
                .map(image -> ImageResponseDTO.ImageResultDTO.builder()
                        .id(image.getId())
                        .keyName(image.getKeyName())
                        .imageUrl(image.getImageUrl())
                        .filePath(image.getFilePath())
                        .contentId(image.getContentId())
                        .uploadedAt(image.getUploadedAt())
                        .build()).toList();

        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(photoAlbum.getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .content(photoAlbum.getContent())
                .place(photoAlbum.getAmateurShow().getPlace())
                .schedule(photoAlbum.getAmateurShow().getSchedule())
                .imageResultDTOs(imageResultDTOs)
                .build();
    }

    @Override
    public List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO> getPhotoAlbumList(Long memberId){ //로그인 구현 시 member로 받도록 수정
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        List<AmateurShow> amateurShows = amateurShowRepository.findAllByMemberId(memberId);

        List<PhotoAlbum> photoAlbums = amateurShows.stream()
                        .flatMap(amateurShow -> photoAlbumRepository.findAllByAmateurShowId(amateurShow.getId()).stream())
                .collect(Collectors.toList());

        List<Image> images = photoAlbums.stream()
                .flatMap(photoAlbum -> imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbum.getId()).stream())
                .collect(Collectors.toList());


        List<Long> photoAlbumIds = images.stream()
                .map(Image::getContentId)
                .collect(Collectors.toSet())
                .stream().toList();

        Map<Long, PhotoAlbum> photoAlbumMap = photoAlbumRepository.findAllById(photoAlbumIds).stream()
                .collect(Collectors.toMap(PhotoAlbum::getId, Function.identity()));

        List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO> singlePhotoAlbumDTOs = images.stream()
                .map(image -> {
                    PhotoAlbum album = photoAlbumMap.get(image.getContentId());
                    if (album == null) {
                        throw new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND);
                    }

                    return PhotoAlbumResponseDTO.SinglePhotoAlbumDTO.builder()
                            .photoAlbumId(album.getId())
                            .amateurShowName(album.getAmateurShow().getName())
                            .place(album.getAmateurShow().getPlace())
                            .imageUrl(image.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return singlePhotoAlbumDTOs;

    }
}
