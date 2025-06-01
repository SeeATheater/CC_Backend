package cc.backend.amateurShow.service.amateurShowService;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.entity.*;
import cc.backend.amateurShow.repository.*;
import cc.backend.amateurShow.converter.AmateurConverter;
import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    // 소극장 공연 단건 조회
    @Override
    public AmateurShowResponseDTO.AmateurShowResult getAmateurShow(Long amateurId) {
        AmateurShow amateurShow = amateurShowRepository.findById(amateurId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        return AmateurConverter.toResponseDTO(amateurShow);
    }
}
