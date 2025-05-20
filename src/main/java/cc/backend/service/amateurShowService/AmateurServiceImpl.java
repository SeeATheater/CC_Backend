package cc.backend.service.amateurShowService;

import cc.backend.converter.AmateurConverter;
import cc.backend.domain.entity.amateur.*;
import cc.backend.domain.entity.member.Member;
import cc.backend.dto.amateurDTO.AmateurEnrollRequestDTO;
import cc.backend.dto.amateurDTO.AmateurEnrollResponseDTO;
import cc.backend.repository.amateurRepository.*;
import cc.backend.s3.FilePath;
import cc.backend.s3.UuidFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AmateurServiceImpl implements AmateurService {

    private final UuidFileService uuidFileService;
    private final AmateurShowRepository amateurShowRepository;
    private final AmateurCastingRepository amateurCastingRepository;
    private final AmateurNoticeRepository amateurNoticeRepository;
    private final AmateurTicketRepository amateurTicketRepository;
    private final AmateurSummaryRepository amateurSummaryRepository;
    private final AmateurStaffRepository amateurStaffRepository;

    @Transactional
    @Override
    public AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Member member,
                                                                       AmateurEnrollRequestDTO requestDTO,
                                                                       MultipartFile posterImage,
                                                                       List<MultipartFile> castingImages,
                                                                       List<MultipartFile> noticeImages) {
        // 포스터 이미지
        String posterUrl = (posterImage != null) ?
                uuidFileService.createFile(posterImage, FilePath.AMATEUR).getFileUrl() : null;

        // 캐스팅 이미지
        List<String> castingUrls = (castingImages != null) ?
                castingImages.stream()
                        .map(file->uuidFileService.createFile(file, FilePath.AMATEUR_CASTING).getFileUrl())
                        .toList() : null;

        // 공지사항 이미지
        List<String> noticeUrls = (noticeImages != null) ?
                noticeImages.stream()
                        .map(file->uuidFileService.createFile(file, FilePath.AMATEUR_NOTICE).getFileUrl())
                        .toList() : null;

        AmateurShow amateurShow = AmateurConverter.toAmateurShowEntity(member, requestDTO, posterUrl);
        amateurShowRepository.save(amateurShow);

        // 나머지도 저장
        saveRelatedEntity(requestDTO, amateurShow, castingUrls, noticeUrls);

        // response
        return AmateurConverter.toAmateurShowDTO(amateurShow);
    }

    private void saveRelatedEntity(AmateurEnrollRequestDTO requestDTO, AmateurShow amateurShow, List<String> castingUrls, List<String> noticeUrls) {

        // 캐스팅
        List<AmateurCasting> castings = AmateurConverter.toAmateurCastingEntity(requestDTO.getCasting(), castingUrls, amateurShow);
        if(!castings.isEmpty()) {
            amateurCastingRepository.saveAll(castings);
        }

        // 공지사항
        AmateurNotice amateurNotice = AmateurConverter.toAmateurNoticeEntity(requestDTO.getNoticeContent(), noticeUrls, amateurShow);
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
