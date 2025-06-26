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
import cc.backend.event.entity.NewShowEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ApplicationEventPublisher eventPublisher; //이벤트 생성

    // 소극장 공연 등록
    @Transactional
    @Override
    public AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Long memberId,
                                                                   AmateurEnrollRequestDTO requestDTO) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        AmateurShow amateurShow = AmateurConverter.toAmateurShowEntity(member, requestDTO);
        amateurShowRepository.save(amateurShow);

        // 나머지도 저장
        saveRelatedEntity(requestDTO, amateurShow);

        // 좋아요한 멤버리스트
        List<MemberLike> memberLikers = memberLikeRepository.findByPerformerId(memberId);
        // 좋아요한 멤버가 한 명 이상일 때만
        if(!memberLikers.isEmpty()) {
            List<Member> likers = memberLikers.stream()
                    .map(MemberLike::getLiker)
                    .collect(Collectors.toList());

            eventPublisher.publishEvent(new NewShowEvent(amateurShow.getId(), memberId, likers));   //공연등록 이벤트 생성
        }

        // response
        return AmateurConverter.toAmateurEnrollDTO(amateurShow);
    }

    private void saveRelatedEntity(AmateurEnrollRequestDTO requestDTO, AmateurShow amateurShow) {

        // 캐스팅
        List<AmateurCasting> castings = AmateurConverter.toAmateurCastingEntity(requestDTO.getCasting(), amateurShow);
        if(!castings.isEmpty()) {
            amateurCastingRepository.saveAll(castings);
        }

        // 공지사항
        AmateurNotice amateurNotice = AmateurConverter.toAmateurNoticeEntity(requestDTO.getNotice(), amateurShow);
        if (amateurNotice != null) {
            amateurNoticeRepository.save(amateurNotice);
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
    public AmateurEnrollResponseDTO.AmateurEnrollResult updateShow(Long showId, AmateurUpdateRequestDTO requestDTO) {
        AmateurShow amateurShow = amateurShowRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

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
    public void deleteShow(Long amateurShowId) {
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));
        amateurShowRepository.delete(amateurShow);
    }

    // 소극장 공연 단건 조회
    @Override
    public AmateurShowResponseDTO.AmateurShowResult getAmateurShow(Long amateurShowId) {
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        return AmateurConverter.toResponseDTO(amateurShow);
    }

    // 오늘 진행하는 소극장 공연 리스트 조회
    @Override
    public List<AmateurShowResponseDTO.AmateurShowList> getShowToday() {
        LocalDate today = LocalDate.now();
        List<AmateurShow> allShows = amateurShowRepository.findAll();

        // 오늘 날짜를 가진 회차가 있는 공연만
        return allShows.stream()
                .filter(show -> show.getAmateurRounds().stream()
                        .anyMatch(round -> round.getPerformanceDateTime().toLocalDate().equals(today)))
                .distinct()
                .map(show -> AmateurShowResponseDTO.AmateurShowList.builder()
                        .amateurShowId(show.getId())
                        .name(show.getName())
                        .place(show.getPlace())
                        .schedule(show.getSchedule())
                        .posterImageUrl(show.getPosterImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // schedule 파싱하기
    private Optional<LocalDate[]> parseSchedule(String schedule) {
        try {
            if (schedule == null || !schedule.contains("~")) return Optional.empty();

            String[] parts = schedule.split("~"); // ~ 기준으로 자르고
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

            LocalDate start = LocalDate.parse(parts[0].trim(), formatter); // 시작일
            LocalDate end = LocalDate.parse(parts[1].trim(), formatter); // 종료일

            return Optional.of(new LocalDate[]{start, end});
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // 현재 진행중인 소극장 공연 리스트 조회
    @Override
    public Page<AmateurShowResponseDTO.AmateurShowList> getShowOngoing(Pageable pageable) {
        LocalDate today = LocalDate.now();
        List<AmateurShow> allShows = amateurShowRepository.findAll();

        List<AmateurShowResponseDTO.AmateurShowList> result = allShows.stream()
                // 오늘 날짜가 schedule 기간 내에 포함된 공연 필터링
                .filter(show -> parseSchedule(show.getSchedule())
                        .map(dates -> !today.isBefore(dates[0]) && !today.isAfter(dates[1]))
                        .orElse(false))
                .map(show -> AmateurShowResponseDTO.AmateurShowList.builder()
                        .amateurShowId(show.getId())
                        .name(show.getName())
                        .place(show.getPlace())
                        .schedule(show.getSchedule())
                        .posterImageUrl(show.getPosterImageUrl())
                        .build())
                // 공연 시작일 기준 오름차순 정렬
                .sorted(Comparator.comparing(show -> parseSchedule(show.getSchedule())
                        .map(dates -> dates[0])
                        .orElse(LocalDate.MAX)))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), result.size());
        return new PageImpl<>(result.subList(start, end), pageable, result.size());
    }

    // 소극장 공연 랭킹 리스트 조회
    @Override
    public List<AmateurShowResponseDTO.AmateurShowList> getShowRanking() {
        LocalDate today = LocalDate.now();
        List<AmateurShow> shows = amateurShowRepository.findAll();

        return shows.stream()
                .filter(show -> parseSchedule(show.getSchedule())
                        .map(dates -> !today.isAfter(dates[1]))  // 종료일이 오늘 이후인 경우만
                        .orElse(false))
                .sorted(Comparator
                        .comparing(AmateurShow::getTotalSoldTicket, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(show -> parseSchedule(show.getSchedule())
                                .map(dates -> dates[0])
                                .orElse(LocalDate.MAX)))
                .limit(10)
                .map(show -> AmateurShowResponseDTO.AmateurShowList.builder()
                        .amateurShowId(show.getId())
                        .name(show.getName())
                        .place(show.getPlace())
                        .schedule(show.getSchedule())
                        .posterImageUrl(show.getPosterImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // 오늘 마감인 소극장 공연 리스트 조회
    @Override
    public List<AmateurShowResponseDTO.AmateurShowList> getShowClosing() {
        List<AmateurShow> allShows = amateurShowRepository.findAll();
        LocalDate today = LocalDate.now();

        List<AmateurShowResponseDTO.AmateurShowList> result = new ArrayList<>();

        for (AmateurShow show : allShows) {
            // 각 공연의 회차들 중 젤 마지막 회차 날짜 구하기
            Optional<LocalDate> lastDate = show.getAmateurRounds().stream()
                    .map(r -> r.getPerformanceDateTime().toLocalDate()) // 회차 날짜만 추출
                    .max(Comparator.naturalOrder()); // 젤 늦은 날짜 추출

            if (lastDate.isPresent() && lastDate.get().isEqual(today)) { // 마지막 회차 날짜가 오늘인 경우
                result.add(AmateurShowResponseDTO.AmateurShowList.builder()
                        .amateurShowId(show.getId())
                        .name(show.getName())
                        .place(show.getPlace())
                        .schedule(show.getSchedule())
                        .posterImageUrl(show.getPosterImageUrl())
                        .build());
            }
        }

        return result;
    }
}