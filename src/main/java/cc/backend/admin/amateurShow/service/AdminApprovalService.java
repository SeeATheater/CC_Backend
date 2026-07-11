package cc.backend.admin.amateurShow.service;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminApprovalListResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.kafka.service.OutboxService;
import cc.backend.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminApprovalService {

    private final AmateurShowRepository amateurShowRepository;
    private final OutboxService outboxService;

    @Transactional
    public AdminAmateurShowSummaryResponseDTO approveShow(Long showId) {
        AmateurShow show = amateurShowRepository.findByIdForUpdate(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        if (show.getApprovalStatus() == ApprovalStatus.APPROVED) {
            throw new GeneralException(ErrorStatus.AMATEURSHOW_ALREADY_APPROVED);
        }

        show.approve();

        Member performer  = show.getMember();

        // 공연 승인 이벤트를 같은 트랜잭션에서 Outbox에 저장
        outboxService.appendOutboxEvent(
                ApprovalShowEvent.create(show.getId(), performer.getId()),
                "approval-show-topic",
                show.getId().toString()
        );

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    @Transactional
    public AdminAmateurShowSummaryResponseDTO rejectShow(Long showId, AdminAmateurShowRejectRequestDTO dto) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.reject(dto.getRejectReason());

        Member member  = show.getMember();
        // 공연 반려 이벤트를 같은 트랜잭션에서 Outbox에 저장
        outboxService.appendOutboxEvent(
                RejectShowEvent.create(
                        show.getId(),
                        member.getId(),
                        show.getRejectReason()
                ),
                "reject-show-topic",
                show.getId().toString()
        );

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    public PageResponse<AdminApprovalListResponseDTO> getApprovalList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<AmateurShow> pageResult =
                (keyword != null && !keyword.isBlank())
                        ? amateurShowRepository.findByNameContainingIgnoreCase(keyword, pageable)
                        : amateurShowRepository.findAll(pageable);

        Page<AdminApprovalListResponseDTO> dtoPage = pageResult.map(this::toApprovalDto);

        return PageResponse.of(dtoPage);
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
