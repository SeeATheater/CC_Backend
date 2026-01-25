package cc.backend.admin.amateurShow.service;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminApprovalListResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.event.entity.ApproveShowEvent;
import cc.backend.event.entity.RejectShowEvent;
import cc.backend.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminApprovalService {

    private final AmateurShowRepository amateurShowRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AdminAmateurShowSummaryResponseDTO approveShow(Long showId) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.approve();

        Member member  = show.getMember();
        eventPublisher.publishEvent(new ApproveShowEvent(show, member));   //공연등록 승인 이벤트 생성

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    @Transactional
    public AdminAmateurShowSummaryResponseDTO rejectShow(Long showId, AdminAmateurShowRejectRequestDTO dto) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.reject(dto.getRejectReason());

        Member member  = show.getMember();
        eventPublisher.publishEvent(new RejectShowEvent(show, member));   //공연등록 반려 이벤트 생성

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    public Page<AdminApprovalListResponseDTO> getApprovalList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<AmateurShow> pageResult =
                (keyword != null && !keyword.isBlank())
                        ? amateurShowRepository.findByNameContainingIgnoreCase(keyword, pageable)
                        : amateurShowRepository.findAll(pageable);

        List<AdminApprovalListResponseDTO> content = pageResult.getContent().stream()
                .map(this::toApprovalDto)
                .toList();

        return new PageImpl<>(content, pageable, pageResult.getTotalElements());
    }

    private AdminApprovalListResponseDTO toApprovalDto(AmateurShow show) {
        Member registrant = show.getMember();

        return AdminApprovalListResponseDTO.builder()
                .showId(show.getId())
                .username(registrant.getUsername())
                .memberName(registrant.getName())
                .email(registrant.getEmail())
                .phone(registrant.getPhone())
                .showName(show.getName())
                .approvalStatus(show.getApprovalStatus().name())
                .build();
    }

}
