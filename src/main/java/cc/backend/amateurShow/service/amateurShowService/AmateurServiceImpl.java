package cc.backend.amateurShow.service.amateurShowService;

import cc.backend.amateurShow.entity.*;
import cc.backend.amateurShow.repository.*;
import cc.backend.amateurShow.converter.AmateurConverter;
import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmateurServiceImpl implements AmateurService {

    private final AmateurShowRepository amateurShowRepository;
    private final AmateurCastingRepository amateurCastingRepository;
    private final AmateurNoticeRepository amateurNoticeRepository;
    private final AmateurTicketRepository amateurTicketRepository;
    private final AmateurSummaryRepository amateurSummaryRepository;
    private final AmateurStaffRepository amateurStaffRepository;

    @Transactional
    @Override
    public AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Member member,
                                                                   AmateurEnrollRequestDTO requestDTO) {
//        // 포스터 이미지
//        String posterUrl = (posterImage != null) ?
//                uuidFileService.createFile(posterImage, FilePath.AMATEUR).getFileUrl() : null;
//
//        // 캐스팅 이미지
//        List<String> castingUrls = (castingImages != null) ?
//                castingImages.stream()
//                        .map(file->uuidFileService.createFile(file, FilePath.AMATEUR_CASTING).getFileUrl())
//                        .toList() : null;
//
//        // 공지사항 이미지
//        List<String> noticeUrls = (noticeImages != null) ?
//                noticeImages.stream()
//                        .map(file->uuidFileService.createFile(file, FilePath.AMATEUR_NOTICE).getFileUrl())
//                        .toList() : null;

        AmateurShow amateurShow = AmateurConverter.toAmateurShowEntity(member, requestDTO);
        amateurShowRepository.save(amateurShow);

        // 나머지도 저장
        saveRelatedEntity(requestDTO, amateurShow);

        // response
        return AmateurConverter.toAmateurShowDTO(amateurShow);
    }

    private void saveRelatedEntity(AmateurEnrollRequestDTO requestDTO, AmateurShow amateurShow) {

        // 캐스팅
        List<AmateurCasting> castings = AmateurConverter.toAmateurCastingEntity(requestDTO.getCasting(), amateurShow);
        if(!castings.isEmpty()) {
            amateurCastingRepository.saveAll(castings);
        }

        // 공지사항
        AmateurNotice amateurNotice = AmateurConverter.toAmateurNoticeEntity(requestDTO.getNoticeContent(), amateurShow);
        if (amateurNotice != null) {
            amateurNoticeRepository.save(amateurNotice);
        }

        // 티켓
        List<AmateurTicket> tickets = AmateurConverter.toAmateurTicketEntity(requestDTO, amateurShow);
        if (!tickets.isEmpty()) {
            amateurTicketRepository.saveAll(tickets);
        }

        // 줄거리
        AmateurSummary amateurSummary = AmateurConverter.toAmateurSummaryEntity(requestDTO.getSummaryContent(), amateurShow);
        if (amateurSummary != null) {
            amateurSummaryRepository.save(amateurSummary);
        }

        // 스태프
        List<AmateurStaff> amateurStaff = AmateurConverter.toAmateurStaffEntity(requestDTO.getStaff(), amateurShow);
        if (!amateurStaff.isEmpty()) {
            amateurStaffRepository.saveAll(amateurStaff);
        }
    }
}
