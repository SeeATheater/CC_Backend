package cc.backend.photoAlbum.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.config.s3.S3Service;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import cc.backend.image.service.ImageService;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.photoAlbum.dto.PerformerShowListResponseDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumRequestDTO;
import cc.backend.photoAlbum.dto.PhotoAlbumResponseDTO;
import cc.backend.photoAlbum.entity.PhotoAlbum;
import cc.backend.photoAlbum.repository.PhotoAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final S3Service s3Service;

    @Override
    @Transactional
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Long amateurShowId = requestDTO.getAmateurShowId();
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        PhotoAlbum newPhotoAlbum = photoAlbumRepository.save(PhotoAlbum.builder()
                .amateurShow(amateurShow)
                .content(requestDTO.getContent())
                .build());

        List<ImageRequestDTO.FullImageRequestDTO> fullImageRequestDTOs = requestDTO.getImageRequestDTOs()
                .stream()
                .map(dto-> ImageRequestDTO.FullImageRequestDTO.builder()
                        .keyName(dto.getKeyName())
                        .imageUrl(dto.getImageUrl())
                        .filePath(FilePath.photoAlbum)
                        .contentId(newPhotoAlbum.getId())
                        .memberId(member.getId())
                        .build()).toList();

        List<ImageResponseDTO.ImageResultDTO> imageResultDTOs = imageService.saveImages(memberId, fullImageRequestDTOs);

        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(newPhotoAlbum.getId())
                .amateurShowName(newPhotoAlbum.getAmateurShow().getName())
                .content(newPhotoAlbum.getContent())
                .detailAddress(newPhotoAlbum.getAmateurShow().getDetailAddress())
                .schedule(newPhotoAlbum.getAmateurShow().getSchedule())
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
                .detailAddress(photoAlbum.getAmateurShow().getDetailAddress())
                .schedule(photoAlbum.getAmateurShow().getSchedule())
                .imageResultDTOs(imageResultDTOs)
                .build();
    }

    @Override
    public List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO> getPhotoAlbumList(Long memberId, Long performerId){
        //로그인 검사
        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        Member performer = memberRepository.findById(performerId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<AmateurShow> amateurShows = amateurShowRepository.findAllByMemberIdOrderByUpdatedAtDesc(performerId);

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
                            .detailAddress(album.getAmateurShow().getDetailAddress())
                            .imageUrl(image.getImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return singlePhotoAlbumDTOs;

    }

    @Override
    @Transactional
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO updatePhotoAlbum(Long photoAlbumId, PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId){
        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        AmateurShow amateurShow = amateurShowRepository.findById(requestDTO.getAmateurShowId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        PhotoAlbum updatedPhotoAlbum = photoAlbum.updatePhotoAlbum(requestDTO.getContent(), amateurShow);

        // 기존 이미지들 가져오기
        List<Image> existingImages = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, updatedPhotoAlbum.getId());

        // 프론트에서 받은 이미지 url 목록
        List<String> newImageUrls = requestDTO.getImageRequestDTOs().stream()
                .map(dto->dto.getImageUrl()).toList();

        // 수정 후 사라진 사진들 - 삭제 대상 찾기
        List<Image> toDelete = existingImages.stream()
                .filter(img -> !newImageUrls.contains(img.getImageUrl()))
                .toList();

        // 삭제
        toDelete.forEach(image -> {
            imageService.deleteImage(image.getId(), memberId);
        });

        List<String> existingUrls = existingImages.stream()
                .map(Image::getImageUrl)
                .toList();
        List<ImageRequestDTO.PartialImageRequestDTO> imageDTOs = requestDTO.getImageRequestDTOs();
        // 수정 후 새로 생긴 사진들 - 저장 대상 찾기
        List<ImageRequestDTO.FullImageRequestDTO> toAdd = imageDTOs.stream()
                .filter(imageDTO -> !existingUrls.contains(imageDTO.getImageUrl()))
                .map(imageDTO -> ImageRequestDTO.FullImageRequestDTO.builder()
                                .imageUrl(imageDTO.getImageUrl())
                                .keyName(imageDTO.getKeyName())
                                .filePath(FilePath.photoAlbum)
                                .contentId(updatedPhotoAlbum.getId())
                                .memberId(memberId)
                                .build()).toList();

        imageService.saveImages(memberId, toAdd);

        List<ImageResponseDTO.ImageResultDTO> imageResultDTOs =
                imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, updatedPhotoAlbum.getId())
                        .stream()
                        .map(image -> ImageResponseDTO.ImageResultDTO.builder()
                                .id(image.getId())
                                .imageUrl(image.getImageUrl())
                                .keyName(image.getKeyName())
                                .filePath(FilePath.photoAlbum)
                                .contentId(image.getContentId())
                                .memberId(image.getMemberId())
                                .uploadedAt(image.getUploadedAt())
                                .build())
                        .collect(Collectors.toList());

        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(updatedPhotoAlbum.getId())
                .amateurShowName(updatedPhotoAlbum.getAmateurShow().getName())
                .content(updatedPhotoAlbum.getContent())
                .detailAddress(updatedPhotoAlbum.getAmateurShow().getDetailAddress())
                .schedule(updatedPhotoAlbum.getAmateurShow().getSchedule())
                .imageResultDTOs(imageResultDTOs)
                .build();
    }

    @Override
    @Transactional
    public String deletePhotoAlbum(Long photoAlbumId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));
        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        if(!photoAlbum.getAmateurShow().getMember().getId().equals(memberId)){
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbumId);
        images.forEach(image -> imageService.deleteImage(image.getId(), memberId));

        photoAlbumRepository.delete(photoAlbum);

        return "삭제가 완료되었습니다.";
    }

    @Override
    public List<PhotoAlbumResponseDTO.MemberPhotoAlbumDTO> getAllPhotoAlbumList(){

        List<PhotoAlbum> photoAlbums = photoAlbumRepository.findAllByOrderByUpdatedAtDesc();
        List<PhotoAlbumResponseDTO.MemberPhotoAlbumDTO> memberPhotoAlbumDTOs = photoAlbums.stream()
                .map(photoAlbum -> {
                    String imageUrl = imageRepository.findAllByFilePathAndContentId(
                                    FilePath.photoAlbum,
                                    photoAlbum.getId()
                            ).stream()
                            .findFirst()
                            .map(Image::getImageUrl)
                            .orElse(null);

                    return PhotoAlbumResponseDTO.MemberPhotoAlbumDTO.builder()
                            .photoAlbumId(photoAlbum.getId())
                            .memberId(photoAlbum.getAmateurShow().getMember().getId())
                            .memberName(photoAlbum.getAmateurShow().getMember().getName())
                            .amateurShowName(photoAlbum.getAmateurShow().getName())
                            .imageUrl(imageUrl)
                            .build();
                })
                .collect(Collectors.toList());

        return memberPhotoAlbumDTOs;
    }

    // hy) 사진첩 옆에서 쓰이는 공연진 - 공연 목록보기
    @Override
    public PerformerShowListResponseDTO getPerformerShows(Long memberId, Pageable pageable) {

        Slice<AmateurShow> slice = amateurShowRepository.findByMember_IdOrderByIdDesc(memberId, pageable);
        long total = amateurShowRepository.countByMember_Id(memberId); // 총 개수

        List<PerformerShowListResponseDTO.ShowList> showLists =
                slice.map(PerformerShowListResponseDTO.ShowList::from).getContent();

        return PerformerShowListResponseDTO.builder()
                .totalCount(total)
                .shows(showLists)
                .build();
    }
}
