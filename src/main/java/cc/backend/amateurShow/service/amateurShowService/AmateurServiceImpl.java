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
import cc.backend.event.entity.CommentEvent;
import cc.backend.event.entity.NewShowEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AmateurShow newAmateurShow = amateurShowRepository.save(amateurShow);

        // 나머지도 저장
        saveRelatedEntity(requestDTO, amateurShow);

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
}