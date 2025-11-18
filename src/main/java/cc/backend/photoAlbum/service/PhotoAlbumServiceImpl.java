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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cc.backend.amateurShow.converter.AmateurConverter.mergeSchedule;
import static java.util.stream.Collectors.toList;


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
    public PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO createPhotoAlbum(PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId){
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
                        .filePath(FilePath.photoAlbum)
                        .contentId(newPhotoAlbum.getId())
                        .memberId(member.getId())
                        .build()).toList();

        List<ImageResponseDTO.ImageResultWithPresignedUrlDTO> imageResultDTOs = imageService.saveImages(memberId, fullImageRequestDTOs);

        LocalDate start = newPhotoAlbum.getAmateurShow().getStart();
        LocalDate end = newPhotoAlbum.getAmateurShow().getEnd();
        String schedule = mergeSchedule(start, end);

        return PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO.builder()
                .performerName(amateurShow.getPerformerName())
                .photoAlbumId(newPhotoAlbum.getId())
                .amateurShowName(newPhotoAlbum.getAmateurShow().getName())
                .content(newPhotoAlbum.getContent())
                .detailAddress(newPhotoAlbum.getAmateurShow().getDetailAddress())
                .schedule(schedule)
                .imageResultWithPresignedUrlDTOs(imageResultDTOs)
                .build();
    }

    @Override
    public PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO getPhotoAlbum(Long photoAlbumId, Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbumId);

        List<ImageResponseDTO.ImageResultWithPresignedUrlDTO> imageResultDTOs
                = imageService.getImages(images, memberId);

        LocalDate start = photoAlbum.getAmateurShow().getStart();
        LocalDate end = photoAlbum.getAmateurShow().getEnd();
        String schedule = mergeSchedule(start, end);

        return PhotoAlbumResponseDTO.PhotoAlbumResultWithPresignedUrlDTO.builder()
                .photoAlbumId(photoAlbum.getId())
                .amateurShowName(photoAlbum.getAmateurShow().getName())
                .performerName(photoAlbum.getAmateurShow().getPerformerName())
                .content(photoAlbum.getContent())
                .detailAddress(photoAlbum.getAmateurShow().getDetailAddress())
                .schedule(schedule)
                .imageResultWithPresignedUrlDTOs(imageResultDTOs)
                .build();
    }

    @Override
    public PhotoAlbumResponseDTO.PerformerPhotoAlbumDTO getPhotoAlbumList(Long memberId, Long performerId, Long cursorId, int pageSize){
        //로그인 검사
        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));
        Member performer = memberRepository.findById(performerId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 사진첩 단위 조회 (커서 기반)
        List<PhotoAlbum> photoAlbums = photoAlbumRepository.findByPerformerWithCursor
                (performerId, cursorId, PageRequest.of(0, pageSize + 1)
        );

        //다음 커서 설정
        boolean hasNext = photoAlbums.size() > pageSize;
        Long nextCursor = hasNext ? photoAlbums.get(pageSize - 1).getId() : null;

        // 실제 응답할 데이터만 자르기(pageSize 보다 1개 더 가져옴)
        List<PhotoAlbum> limitedAlbums = photoAlbums.stream()
                .limit(pageSize)
                .toList();

        // 대표 이미지 가져오기
        List<Long> albumIds = limitedAlbums.stream()
                .map(PhotoAlbum::getId)
                .toList();

        // 쿼리 한번에 다 가져오기
        Map<Long, Image> albumImageMap = imageRepository.findFirstByContentIds(albumIds, FilePath.photoAlbum)
                .stream()
                .collect(Collectors.toMap(Image::getContentId, Function.identity()));

        // DTO 변환
        List<PhotoAlbumResponseDTO.SinglePhotoAlbumDTO> singlePhotoAlbumDTOs = limitedAlbums.stream()
                .map(album -> {
                    Image coverImage = albumImageMap.get(album.getId());
                    return PhotoAlbumResponseDTO.SinglePhotoAlbumDTO.builder()
                            .photoAlbumId(album.getId())
                            .amateurShowName(album.getAmateurShow().getName())
                            .performerName(performer.getName())
                            .detailAddress(album.getAmateurShow().getDetailAddress())
                            .imageResultWithPresignedUrlDTO(
                                    coverImage != null
                                            ? imageService.getImages(List.of(coverImage), memberId).get(0)
                                            : null
                            )
                            .build();
                })
                .toList();

        return PhotoAlbumResponseDTO.PerformerPhotoAlbumDTO.builder()
                .singlePhotoAlbumDTOs(singlePhotoAlbumDTOs)
                .performerName(performer.getName())
                .number(singlePhotoAlbumDTOs.size())
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    @Transactional
    public PhotoAlbumResponseDTO.PhotoAlbumResultDTO updatePhotoAlbum(Long photoAlbumId, PhotoAlbumRequestDTO.CreatePhotoAlbumDTO requestDTO, Long memberId){
        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        PhotoAlbum photoAlbum = photoAlbumRepository.findById(photoAlbumId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PHOTOALBUM_NOT_FOUND));

        AmateurShow amateurShow = amateurShowRepository.findById(requestDTO.getAmateurShowId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        PhotoAlbum updatedPhotoAlbum = photoAlbum.updatePhotoAlbum(requestDTO.getContent(), amateurShow);

        // 기존 이미지들 가져오기
        List<Image> existingImages = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, updatedPhotoAlbum.getId());

        // 프론트에서 받은 이미지 url 목록
        Set<String> newKeyNames = requestDTO.getImageRequestDTOs().stream()
                .map(ImageRequestDTO.PartialImageRequestDTO::getKeyName)
                .collect(Collectors.toSet());

        // 수정 후 사라진 사진들 - 삭제 대상 찾기
        List<Image> toDelete = existingImages.stream()
                .filter(img -> !newKeyNames.contains(img.getImageUrl()))
                .toList();

        // 삭제
        toDelete.forEach(image -> {
            imageService.deleteImage(image.getId(), memberId);
        });

        // 삭제 후 남아있는 기존 사진
        Set<String> existingUrls = existingImages.stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toSet());

        List<ImageRequestDTO.PartialImageRequestDTO> imageDTOs = requestDTO.getImageRequestDTOs();

        // 수정 후 새로 생긴 사진들 - 기존 사진 셋에 없는 requestDTO 사진들은 추가 저장
        List<ImageRequestDTO.FullImageRequestDTO> toAdd = imageDTOs.stream()
                .filter(imageDTO -> !existingUrls.contains(imageDTO.getKeyName()))
                .map(imageDTO -> ImageRequestDTO.FullImageRequestDTO.builder()
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
                                .keyName(image.getKeyName())
                                .filePath(FilePath.photoAlbum)
                                .contentId(image.getContentId())
                                .memberId(image.getMemberId())
                                .uploadedAt(image.getUploadedAt())
                                .build())
                        .collect(toList());

        LocalDate start = updatedPhotoAlbum.getAmateurShow().getStart();
        LocalDate end = updatedPhotoAlbum.getAmateurShow().getEnd();
        String schedule = mergeSchedule(start, end);

        return PhotoAlbumResponseDTO.PhotoAlbumResultDTO.builder()
                .photoAlbumId(updatedPhotoAlbum.getId())
                .amateurShowName(updatedPhotoAlbum.getAmateurShow().getName())
                .performerName(updatedPhotoAlbum.getAmateurShow().getPerformerName())
                .content(updatedPhotoAlbum.getContent())
                .detailAddress(updatedPhotoAlbum.getAmateurShow().getDetailAddress())
                .schedule(schedule)
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

        AmateurShow show = photoAlbum.getAmateurShow();
        if(show==null){
            throw new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND);
        }

        if(!show.getMember().getId().equals(memberId)){
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.photoAlbum, photoAlbumId);
        images.forEach(image -> imageService.deleteImage(image.getId(), memberId));

        photoAlbumRepository.delete(photoAlbum);

        return "삭제가 완료되었습니다.";
    }

    @Override
    public PhotoAlbumResponseDTO.ScrollMemberPhotoAlbumDTO getAllPhotoAlbumList(Long cursorId, int size){

        Pageable pageable = PageRequest.of(0, size + 1);
        List<PhotoAlbum> albums = photoAlbumRepository.findNextAlbums(cursorId, pageable);

        // 실제 응답할 데이터만 자르기(size 보다 1개 더 가져옴)
        List<PhotoAlbum> limitedAlbums = albums.stream()
                .limit(size)
                .toList();

        // 대표 이미지 조회 (N+1 방지)
        List<Long> albumIds = limitedAlbums.stream()
                .map(PhotoAlbum::getId)
                .toList();

        Map<Long, Image> albumImageMap = imageRepository.findFirstByContentIds(albumIds, FilePath.photoAlbum)
                .stream()
                .collect(Collectors.toMap(Image::getContentId, Function.identity()));

        //DTO 변환
        List<PhotoAlbumResponseDTO.MemberPhotoAlbumDTO> dtoList = limitedAlbums.stream()
                .map(album -> {
                    Image coverImage = albumImageMap.get(album.getId());
                    return PhotoAlbumResponseDTO.MemberPhotoAlbumDTO.builder()
                            .photoAlbumId(album.getId())
                            .memberId(album.getAmateurShow().getMember().getId())
                            .performerName(album.getAmateurShow().getMember().getName())
                            .amateurShowName(album.getAmateurShow().getName())
                            .imageUrl(coverImage != null ? coverImage.getImageUrl() : null)
                            .build();
                })
                .toList();

        // 다음 커서 설정
        boolean hasNext = albums.size() > size;
        Long nextCursor = hasNext ? limitedAlbums.get(limitedAlbums.size() - 1).getId() : null;

        return PhotoAlbumResponseDTO.ScrollMemberPhotoAlbumDTO.builder()
                .photoAlbumDTOs(dtoList)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
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
