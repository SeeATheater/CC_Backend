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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
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
        AmateurShow amateurShow = AmateurConverter.toAmateurShowEntity(member, requestDTO);
        AmateurShow newAmateurShow = amateurShowRepository.save(amateurShow);

        // 나머지도 저장
        saveRelatedEntity(requestDTO, newAmateurShow);

        //posterImageUrl 필드는 이미 Converter에서 기입, 포스터 사진 DB에만 저장(1개만)
        ImageRequestDTO.PosterImageRequestDTO dto = requestDTO.getPosterImageRequestDTO();
        ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                .keyName(dto.getKeyName())
                .filePath(FilePath.amateurShow)
                .contentId(newAmateurShow.getId())
                .memberId(memberId)
                .build();
        imageService.saveImage(memberId, fullImageRequestDTO);

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
                ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                        .keyName(amateurCasting.getCastingImageKeyName())
                        .filePath(FilePath.amateurShow)
                        .contentId(amateurShow.getId())
                        .memberId(memberId)
                        .build();
                imageService.saveImage(memberId, fullImageRequestDTO);
            });
        }

        // 공지사항
        AmateurNotice amateurNotice = AmateurConverter.toAmateurNoticeEntity(requestDTO.getNotice(), amateurShow);
        if (amateurNotice != null) {
            amateurNoticeRepository.save(amateurNotice);

            ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                    .keyName(requestDTO.getNotice().getNoticeImageRequestDTO().getKeyName())
                    .filePath(FilePath.amateurShow)
                    .contentId(amateurShow.getId())
                    .memberId(memberId)
                    .build();

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
                .orElseThrow(()->new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        AmateurShow amateurShow = amateurShowRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        //포스터 사진 수정
        ImageRequestDTO.PosterImageRequestDTO dto = requestDTO.getPosterImageRequestDTO();
        if (dto != null && dto.getKeyName() != null && !dto.getKeyName().isBlank()) {
            // 현재 포스터 이미지 조회 (show당 1개)
            Image existingImage = imageRepository
                    .findAllByFilePathAndContentId(FilePath.amateurShow, amateurShow.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);

            // 기존 keyName과 다르면 기존 이미지 삭제 후 교체
            if (existingImage != null && !existingImage.getKeyName().equals(dto.getKeyName())) {
                imageService.deleteImage(existingImage.getId(), memberId);
            }

            ImageRequestDTO.FullImageRequestDTO fullImageRequestDTO = ImageRequestDTO.FullImageRequestDTO.builder()
                    .keyName(dto.getKeyName())
                    .filePath(FilePath.amateurShow)
                    .contentId(amateurShow.getId())
                    .memberId(memberId)
                    .build();

            imageService.saveImage(memberId, fullImageRequestDTO);
            //amateurShow 엔티티 내 posterImageUrl 필드 수정
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
    }

    private void updateCasting(AmateurShow show, List<AmateurUpdateRequestDTO.UpdateCasting> dtos) {
        if (dtos == null) return;

        // 기존 캐스팅 리스트 Map화 (id -> entity)
        Map<Long, AmateurCasting> existingMap = show.getAmateurCastingList().stream()
                .collect(Collectors.toMap(AmateurCasting::getId, c -> c));

        List<AmateurCasting> updatedList = new ArrayList<>();

        for (AmateurUpdateRequestDTO.UpdateCasting dto : dtos) {
            if (dto.getCastingId() != null && existingMap.containsKey(dto.getCastingId())) {
                // 기존 객체 수정
                AmateurCasting existing = existingMap.get(dto.getCastingId());
                existing.update(dto);
                updatedList.add(existing);
                existingMap.remove(dto.getCastingId());
            } else {
                // 새 객체 추가
                AmateurCasting newCasting = AmateurConverter.toSingleCasting(dto, show);
                updatedList.add(newCasting);
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
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));
        amateurShowRepository.delete(amateurShow);

        List<Image> images = imageRepository.findAllByFilePathAndContentId(FilePath.amateurShow, amateurShowId);
        images.forEach(image -> imageService.deleteImage(image.getId(), memberId));
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
    public List<AmateurShowResponseDTO.AmateurShowList> getShowToday(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        LocalDate today = LocalDate.now();
        List<AmateurShow> allShows = amateurShowRepository.findAllWithRounds();

        // 오늘 날짜를 가진 회차가 있는 공연만
        return allShows.stream()
                .filter(show -> show.getAmateurRounds().stream()
                        .anyMatch(round -> round.getPerformanceDateTime().toLocalDate().equals(today)))
                .distinct()
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

    // 현재 진행중인 소극장 공연 리스트 조회
    @Override
    public Page<AmateurShowResponseDTO.AmateurShowList> getShowOngoing(Long memberId, Pageable pageable) {
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
        return new PageImpl<>(result.subList(start, end), pageable, result.size());
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
    public List<AmateurShowResponseDTO.AmateurShowList> getIncomingShow (Long memberId){
        memberRepository.findById(memberId).orElseThrow(()-> new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED));

        Collator collator = Collator.getInstance(Locale.KOREAN); //한글 사전식 정렬

        // 종료일이 오늘 이후인 공연만 DB에서 조회
        LocalDate today = LocalDate.now();
        List<AmateurShow> shows = amateurShowRepository.findByEndGreaterThanEqual(today);

        return shows.stream()
                // 시작일 기준 오름차순 → 이름 기준 오름차순(한글 사전식)
                .sorted(Comparator
                        .comparing(AmateurShow::getStart)
                        .thenComparing(AmateurShow::getName, Comparator.nullsLast(collator)))
                .limit(10)
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

    
}