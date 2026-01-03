package cc.backend.amateurShow.service.amateurShowService;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.amateurShow.entity.*;
import cc.backend.amateurShow.repository.*;
import cc.backend.amateurShow.converter.AmateurConverter;
import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.event.entity.NewShowEvent;
import cc.backend.image.DTO.ImageRequestDTO;
import cc.backend.image.DTO.ImageResponseDTO;
import cc.backend.image.FilePath;
import cc.backend.image.entity.Image;
import cc.backend.image.repository.ImageRepository;
import cc.backend.image.service.ImageService;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import cc.backend.ticket.dto.response.ReserveListResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmateurServiceImpl implements AmateurService {

    private final MemberRepository memberRepository;
    private final AmateurShowRepository amateurShowRepository;
    private final AmateurCastingRepository amateurCastingRepository;
    private final AmateurNoticeRepository amateurNoticeRepository;
    private final AmateurTicketRepository amateurTicketRepository;
    private final AmateurStaffRepository amateurStaffRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final ApplicationEventPublisher eventPublisher; //이벤트 생성

    // 소극장 공연 등록
    @Transactional
    @Override
    public AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Long memberId,
                                                                   AmateurEnrollRequestDTO requestDTO) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.PERFORMER) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_PERFORMER);
        }

        AmateurShow amateurShow = AmateurConverter.toAmateurShowEntity(member, requestDTO);
        AmateurShow newAmateurShow = amateurShowRepository.save(amateurShow);

        // 나머지도 저장
        saveRelatedEntity(requestDTO, newAmateurShow);

        //posterImageUrl 필드는 이미 Converter에서 기입, 포스터 사진 DB에만 저장(1개만)
        ImageRequestDTO.PosterImageRequestDTO dto = requestDTO.getPosterImageRequestDTO();

        //poster 이미지는 없으면 에러
        if(dto.getKeyName() == null || dto.getKeyName().isBlank()){
            throw new GeneralException(ErrorStatus.INVALID_S3_KEY);
        }

        ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                .keyName(dto.getKeyName())
                .filePath(FilePath.amateurShow)
                .contentId(newAmateurShow.getId())
                .memberId(memberId)
                .build();

        imageService.saveImageWithImageUrl(memberId, fullImageRequestDTO, Optional.ofNullable(dto.getImageUrl()));


        // 좋아요한 멤버리스트
        List<MemberLike> memberLikers = memberLikeRepository.findByPerformerId(memberId);
        // 좋아요한 멤버가 한 명 이상일 때만
        if(!memberLikers.isEmpty()) {
            List<Member> likers = memberLikers.stream()
                    .map(MemberLike::getLiker)
                    .collect(Collectors.toList());

            eventPublisher.publishEvent(new NewShowEvent(newAmateurShow.getId(), memberId, likers));   //공연등록 이벤트 생성
        }

        // response
        return AmateurConverter.toAmateurEnrollDTO(newAmateurShow);
    }

    private void saveRelatedEntity(AmateurEnrollRequestDTO requestDTO, AmateurShow amateurShow) {

        Long memberId = amateurShow.getMember().getId();
        // 캐스팅
        List<AmateurCasting> castings = AmateurConverter.toAmateurCastingEntity(requestDTO.getCasting(), amateurShow);
        if (!castings.isEmpty()) {
            List<AmateurCasting> amateurCastings = amateurCastingRepository.saveAll(castings);
            // 캐스팅 사진 저장(1개씩)
            amateurCastings.forEach(amateurCasting -> {
                String keyName = amateurCasting.getCastingImageKeyName();
                // keyName 없으면 스킵
                if (keyName == null || keyName.isBlank()) {
                    return;
                }

                ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                        .keyName(amateurCasting.getCastingImageKeyName())
                        .filePath(FilePath.casting)
                        .contentId(amateurCasting.getId())
                        .memberId(memberId)
                        .build();

                imageService.saveImageWithImageUrl(
                        memberId,
                        fullImageRequestDTO,
                        Optional.ofNullable(amateurCasting.getCastingImageUrl())
                );
            });
        }

        // 공지사항
        AmateurNotice amateurNotice = AmateurConverter.toAmateurNoticeEntity(requestDTO.getNotice(), amateurShow);
        if (amateurNotice != null) {
            amateurNoticeRepository.save(amateurNotice);

            ImageRequestDTO.NoticeImageRequestDTO noticeImageDTO = requestDTO.getNotice().getNoticeImageRequestDTO();
            //keyName 비었으면 스킵
            if (noticeImageDTO != null && noticeImageDTO.getKeyName() != null && !noticeImageDTO.getKeyName().isBlank()) {
                ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                        .keyName(noticeImageDTO.getKeyName())
                        .filePath(FilePath.notice)
                        .contentId(amateurNotice.getId())
                        .memberId(memberId)
                        .build();

                imageService.saveImageWithImageUrl(
                        memberId,
                        fullImageRequestDTO,
                        Optional.ofNullable(noticeImageDTO.getImageUrl())
                );
            }
        }

        // 티켓
        List<AmateurTicket> tickets = AmateurConverter.toAmateurTicketEntity(requestDTO, amateurShow);
        if (!tickets.isEmpty()) {
            amateurTicketRepository.saveAll(tickets);
        }

        // 스태프
        List<AmateurStaff> amateurStaff = AmateurConverter.toAmateurStaffEntity(requestDTO.getStaff(), amateurShow);
        if (!amateurStaff.isEmpty()) {
            amateurStaffRepository.saveAll(amateurStaff);
        }

        // 공연 회차
        List<AmateurRounds> rounds = AmateurConverter.toAmateurRoundEntity(requestDTO.getRounds(), amateurShow);
        amateurRoundsRepository.saveAll(rounds);

    }

    // 소극장 공연 수정
    @Transactional
    @Override
    public AmateurEnrollResponseDTO.AmateurEnrollResult updateShow(Long memberId, Long showId, AmateurUpdateRequestDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.PERFORMER) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_PERFORMER);
        }

        AmateurShow amateurShow = amateurShowRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        if (!amateurShow.getMember().getId().equals(memberId)) { // 다른 공연자가 수정 못하게 방지
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        ImageRequestDTO.PosterImageRequestDTO dto = requestDTO.getPosterImageRequestDTO();
        if (dto != null && dto.getKeyName() != null && !dto.getKeyName().isBlank()) {
            imageService.updateShowImage(
                    memberId,
                    dto.getKeyName(),
                    Optional.ofNullable(dto.getImageUrl()),
                    amateurShow.getId(),
                    FilePath.amateurShow
            );

            amateurShow.updatePosterImageUrl(dto.getImageUrl());
        }

        // 기본 정보 업데이트
        amateurShow.updateInfo(requestDTO);

        // Notice 업데이트
        updateNotice(amateurShow, requestDTO.getNotice());

        // Casting 업데이트
        updateCasting(amateurShow, requestDTO.getCasting());

        // Staff 업데이트
        updateStaff(amateurShow, requestDTO.getStaff());

        // Rounds 업데이트
        updateRounds(amateurShow, requestDTO.getRounds());

        // Tickets 업데이트
        updateTickets(amateurShow, requestDTO.getTickets());

        // 변경사항 저장
        amateurShowRepository.save(amateurShow);

        return AmateurConverter.toAmateurEnrollDTO(amateurShow);
    }

    private void updateNotice(AmateurShow amateurShow, AmateurUpdateRequestDTO.UpdateNotice noticeDTO) {
        if (noticeDTO != null) {
            AmateurNotice existing = amateurShow.getAmateurNotice();
            if (existing != null) {
                existing.update(noticeDTO);
            } else {
                AmateurNotice newNotice = AmateurConverter.toAmateurNoticeEntity(noticeDTO, amateurShow);
                if (newNotice != null) {
                    amateurNoticeRepository.save(newNotice);
                }
            }
        }

        //NoticeImage 수정
        if (noticeDTO == null) return;

        AmateurNotice notice = amateurShow.getAmateurNotice();
        ImageRequestDTO.NoticeImageRequestDTO dto = noticeDTO.getNoticeImageRequestDTO();
        if (notice != null && dto != null && dto.getKeyName() != null && !dto.getKeyName().isBlank()){
            imageService.updateShowImage(
                    amateurShow.getMember().getId(),
                    dto.getKeyName(),
                    Optional.ofNullable(dto.getImageUrl()),
                    notice.getId(),
                    FilePath.notice
            );
        }
    }

    private void updateCasting(AmateurShow show, List<AmateurUpdateRequestDTO.UpdateCasting> dtos) {
        if (dtos == null) return;

        // 기존 캐스팅 리스트 Map화 (id -> entity)
        Map<Long, AmateurCasting> existingMap = show.getAmateurCastingList().stream()
                .collect(Collectors.toMap(AmateurCasting::getId, c -> c));

        List<AmateurCasting> updatedList = new ArrayList<>();

        for (AmateurUpdateRequestDTO.UpdateCasting dto : dtos) {
            Long contentId;

            if (dto.getCastingId() != null && existingMap.containsKey(dto.getCastingId())) {
                // 기존 객체 수정
                AmateurCasting existing = existingMap.get(dto.getCastingId());
                existing.update(dto);
                updatedList.add(existing);
                existingMap.remove(dto.getCastingId());
                contentId = existing.getId();
            } else {
                // 새 객체 추가
                AmateurCasting newCasting = AmateurConverter.toSingleCasting(dto, show);
                AmateurCasting savedCasting = amateurCastingRepository.save(newCasting);
                updatedList.add(savedCasting);

                contentId = savedCasting.getId();
            }


            // 캐스팅 이미지 업데이트
            ImageRequestDTO.CastingImageRequestDTO castingDTO = dto.getCastingImageRequestDTO();
            if (castingDTO != null && castingDTO.getKeyName() != null && !castingDTO.getKeyName().isBlank()) {
                imageService.updateShowImage(
                        show.getMember().getId(),
                        castingDTO.getKeyName(),
                        Optional.ofNullable(castingDTO.getImageUrl()),
                        contentId,
                        FilePath.casting
                );
            }
        }

        // 삭제
        for (AmateurCasting toRemove : existingMap.values()) {
            show.getAmateurCastingList().remove(toRemove);
        }

        // 최종 리스트 갱신
        show.getAmateurCastingList().clear();
        show.getAmateurCastingList().addAll(updatedList);

    }

    private void updateStaff(AmateurShow show, List<AmateurUpdateRequestDTO.UpdateStaff> dtos) {
        if (dtos == null) return;
        Map<Long, AmateurStaff> existingMap = show.getAmateurStaffList().stream()
                .collect(Collectors.toMap(AmateurStaff::getId, s -> s));
        List<AmateurStaff> updatedList = new ArrayList<>();

        for (AmateurUpdateRequestDTO.UpdateStaff dto : dtos) {
            if (dto.getStaffId() != null && existingMap.containsKey(dto.getStaffId())) {
                AmateurStaff existing = existingMap.get(dto.getStaffId());
                existing.update(dto);
                updatedList.add(existing);
                existingMap.remove(dto.getStaffId());
            } else {
                AmateurStaff newStaff = AmateurConverter.toSingleStaff(dto, show);
                updatedList.add(newStaff);
            }
        }

        for (AmateurStaff toRemove : existingMap.values()) {
            show.getAmateurStaffList().remove(toRemove);
        }
        show.getAmateurStaffList().clear();
        show.getAmateurStaffList().addAll(updatedList);
    }

    private void updateRounds(AmateurShow show, List<AmateurUpdateRequestDTO.UpdateRounds> dtos) {
        if (dtos == null) return;
        Map<Long, AmateurRounds> existingMap = show.getAmateurRounds().stream()
                .collect(Collectors.toMap(AmateurRounds::getId, r -> r));
        List<AmateurRounds> updatedList = new ArrayList<>();

        for (AmateurUpdateRequestDTO.UpdateRounds dto : dtos) {
            if (dto.getRoundId() != null && existingMap.containsKey(dto.getRoundId())) {
                AmateurRounds existing = existingMap.get(dto.getRoundId());
                existing.update(dto);
                updatedList.add(existing);
                existingMap.remove(dto.getRoundId());
            } else {
                AmateurRounds newRound = AmateurConverter.toSingleRound(dto, show);
                updatedList.add(newRound);
            }
        }

        for (AmateurRounds toRemove : existingMap.values()) {
            show.getAmateurRounds().remove(toRemove);
        }
        show.getAmateurRounds().clear();
        show.getAmateurRounds().addAll(updatedList);
    }

    private void updateTickets(AmateurShow show, List<AmateurUpdateRequestDTO.UpdateTickets> dtos) {
        if (dtos == null) return;
        Map<Long, AmateurTicket> existingMap = show.getAmateurTicketList().stream()
                .collect(Collectors.toMap(AmateurTicket::getId, t -> t));
        List<AmateurTicket> updatedList = new ArrayList<>();

        for (AmateurUpdateRequestDTO.UpdateTickets dto : dtos) {
            if (dto.getTicketId() != null && existingMap.containsKey(dto.getTicketId())) {
                AmateurTicket existing = existingMap.get(dto.getTicketId());
                existing.update(dto);
                updatedList.add(existing);
                existingMap.remove(dto.getTicketId());
            } else {
                AmateurTicket newTicket = AmateurConverter.toSingleTicket(dto, show);
                updatedList.add(newTicket);
            }
        }

        for (AmateurTicket toRemove : existingMap.values()) {
            show.getAmateurTicketList().remove(toRemove);
        }
        show.getAmateurTicketList().clear();
        show.getAmateurTicketList().addAll(updatedList);
    }


    // 소극장 공연 삭제
    @Transactional
    @Override
    public void deleteShow(Long memberId, Long amateurShowId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.PERFORMER) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_PERFORMER);
        }

        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        if (!amateurShow.getMember().getId().equals(memberId)) { // 다른 공연자가 삭제 못하게 방지
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        //포스터 삭제
        Image posterImg = imageRepository.findByFilePathAndContentId(FilePath.amateurShow, amateurShowId);
        imageService.deleteImage(posterImg.getId(), memberId);

        // 공지 삭제
        if (amateurShow.getAmateurNotice() != null) {
            Image noticeImg = imageRepository.findByFilePathAndContentId(FilePath.notice, amateurShow.getAmateurNotice().getId());
            if (noticeImg != null) {
                imageService.deleteImage(noticeImg.getId(), memberId);
            }
        }

        //캐스팅 삭제
        List<Long> castingIds = amateurShow.getAmateurCastingList()
                .stream()
                .map(AmateurCasting::getId)
                .toList();

        if (!castingIds.isEmpty()) {
            List<Image> castingImages =
                    imageRepository.findByFilePathAndContentIdIn(
                            FilePath.casting, castingIds);

            castingImages.forEach(img ->
                    imageService.deleteImage(img.getId(), memberId));
        }

        //amateurShow 삭제
        amateurShowRepository.delete(amateurShow);
    }

    // 소극장 공연 단건 조회
    @Override
    public AmateurShowResponseDTO.AmateurShowResult getAmateurShow(Long memberId, Long amateurShowId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));


        return AmateurConverter.toResponseDTO(amateurShow);
    }

    // 오늘 진행하는 소극장 공연 리스트 조회
    @Override
    public Slice<AmateurShowResponseDTO.AmateurShowList> getShowToday(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        LocalDate today = LocalDate.now();
        List<AmateurShow> allShows = amateurShowRepository.findAllWithRounds();

        // 오늘 날짜를 가진 회차가 있는 공연만
        List<AmateurShowResponseDTO.AmateurShowList> result = allShows.stream()
                .filter(show -> show.getAmateurRounds().stream()
                        .anyMatch(round ->
                                round.getPerformanceDateTime()
                                        .toLocalDate()
                                        .equals(today)
                        )
                )
                .distinct()
                .sorted(Comparator.comparing(AmateurShow::getStart)) // 정렬 필요하면 유지
                .map(show -> {
                    String schedule = AmateurConverter.mergeSchedule(
                            show.getStart(),
                            show.getEnd()
                    );
                    return AmateurShowResponseDTO.AmateurShowList.builder()
                            .amateurShowId(show.getId())
                            .name(show.getName())
                            .detailAddress(show.getDetailAddress())
                            .schedule(schedule)
                            .posterImageUrl(show.getPosterImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        // ===== Slice 페이징 처리 =====
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), result.size());

        List<AmateurShowResponseDTO.AmateurShowList> content = new ArrayList<>();
        if (start < result.size()) {
            content = result.subList(start, end);
        }

        boolean hasNext = end < result.size();

        return new SliceImpl<>(content, pageable, hasNext);
    }

    // 현재 진행중인 소극장 공연 리스트 조회
    @Override
    public Slice<AmateurShowResponseDTO.AmateurShowList> getShowOngoing(Long memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        LocalDate today = LocalDate.now();
        List<AmateurShow> allShows = amateurShowRepository.findAllWithRounds();

        List<AmateurShowResponseDTO.AmateurShowList> result = allShows.stream()
                // 오늘 날짜가 schedule 기간 내에 포함된 공연만 필터링
                .filter(show -> {
                    LocalDate start = show.getStart();
                    LocalDate end = show.getEnd();
                    return start != null && end != null
                            && !today.isBefore(start)
                            && !today.isAfter(end);
                })
                // 공연 시작일 기준 오름차순 정렬
                .sorted(Comparator.comparing(AmateurShow::getStart))
                // DTO로 변환
                .map(show -> {
                    String schedule = AmateurConverter.mergeSchedule(show.getStart(), show.getEnd());
                    return AmateurShowResponseDTO.AmateurShowList.builder()
                            .amateurShowId(show.getId())
                            .name(show.getName())
                            .detailAddress(show.getDetailAddress())
                            .schedule(schedule)
                            .posterImageUrl(show.getPosterImageUrl())
                            .build();
                })
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), result.size());

        List<AmateurShowResponseDTO.AmateurShowList> content = new ArrayList<>();
        if (start < result.size()) {
            content = result.subList(start, end);
        }

        boolean hasNext = end < result.size();

        return new SliceImpl<>(content, pageable, hasNext); // 시그니처 (List<DTO>, pageable, hasNext)
    }

    // 소극장 공연 랭킹 리스트 조회
    @Override
    public List<AmateurShowResponseDTO.AmateurShowList> getShowRanking(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        LocalDate today = LocalDate.now();
        List<AmateurShow> shows = amateurShowRepository.findAllWithRounds();

        return shows.stream()
                // 종료일이 오늘 이후인 공연만 필터링
                .filter(show -> {
                    LocalDate start = show.getStart();
                    LocalDate end = show.getEnd();
                    return start != null && end != null && !today.isAfter(end);
                })
                // 정렬: 판매 티켓 수 내림차순 → 시작일 오름차순
                .sorted(Comparator
                        .comparing(AmateurShow::getTotalSoldTicket, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AmateurShow::getStart))
                .limit(10)
                // DTO 변환
                .map(show -> {
                    String schedule = AmateurConverter.mergeSchedule(show.getStart(), show.getEnd());
                    return AmateurShowResponseDTO.AmateurShowList.builder()
                            .amateurShowId(show.getId())
                            .name(show.getName())
                            .detailAddress(show.getDetailAddress())
                            .schedule(schedule)
                            .posterImageUrl(show.getPosterImageUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 오늘 마감인 소극장 공연 리스트 조회
    @Override
    public List<AmateurShowResponseDTO.AmateurShowList> getShowClosing(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        List<AmateurShow> allShows = amateurShowRepository.findAllWithRounds();
        LocalDate today = LocalDate.now();

        List<AmateurShowResponseDTO.AmateurShowList> result = new ArrayList<>();

        for (AmateurShow show : allShows) {
            // 각 공연의 회차들 중 젤 마지막 회차 날짜 구하기
            Optional<LocalDate> lastDate = show.getAmateurRounds().stream()
                    .map(r -> r.getPerformanceDateTime().toLocalDate()) // 회차 날짜만 추출
                    .max(Comparator.naturalOrder()); // 젤 늦은 날짜 추출

            if (lastDate.isPresent() && lastDate.get().isEqual(today)) { // 마지막 회차 날짜가 오늘인 경우
                String schedule = AmateurConverter.mergeSchedule(show.getStart(), show.getEnd());

                result.add(AmateurShowResponseDTO.AmateurShowList.builder()
                        .amateurShowId(show.getId())
                        .name(show.getName())
                        //.place(show.getPlace())
                        .detailAddress(show.getDetailAddress())
                        .schedule(schedule)
                        .posterImageUrl(show.getPosterImageUrl())
                        .build());
            }
        }

        return result;
    }

    @Override
    public Slice<AmateurShowResponseDTO.MyShowAmateurShowList> getMyAmateurShow(Long memberId, AmateurShowStatus status, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.PERFORMER) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_PERFORMER);
        }

        Slice<AmateurShow> shows;

        if (status == null) {
            shows = amateurShowRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        } else {
            shows = amateurShowRepository.findAllByMemberIdAndStatusOrderByIdDesc(memberId, status, pageable);
        }


        return shows.map(show -> {
            String schedule = AmateurConverter.mergeSchedule(show.getStart(), show.getEnd());
            return AmateurShowResponseDTO.MyShowAmateurShowList.builder()
                    .amateurShowId(show.getId())
                    .name(show.getName())
                    //.place(show.getPlace())
                    .detailAddress(show.getDetailAddress())
                    .schedule(schedule)
                    .posterImageUrl(show.getPosterImageUrl())
                    .status(status)
                    .build();
        });
    }

    @Override
    public List<AmateurShowResponseDTO.AmateurShowList> getRecentlyHotShow (Long memberId){
        memberRepository.findById(memberId).orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));


        // 종료되지 않은 공연 중, 마감이 얼마 안남은 3개
        LocalDate today = LocalDate.now();
        List<AmateurShow> shows = amateurShowRepository.findHotShows(today, PageRequest.of(0, 3));

        return shows.stream()
                .map(show -> AmateurShowResponseDTO.AmateurShowList.builder()
                        .amateurShowId(show.getId())
                        .name(show.getName())
                        .detailAddress(show.getDetailAddress())
                        .schedule(
                                AmateurConverter.mergeSchedule(
                                        show.getStart(),
                                        show.getEnd()
                                )
                        )
                        .posterImageUrl(show.getPosterImageUrl())
                        .build()
                )
                .toList();

    }

    
}