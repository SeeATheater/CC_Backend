package cc.backend.admin.amateurShow.service;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminApprovalListResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.member.entity.Member;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowProducer;
import cc.backend.notice.event.ApproveCommitEvent;
import cc.backend.notice.event.RejectCommitEvent;
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
    private final ApprovalShowProducer approvalShowProducer;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AdminAmateurShowSummaryResponseDTO approveShow(Long showId) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.approve();

        Member performer  = show.getMember();

        // 등록 승인 커밋 트랜잭션 이벤트 발행
        eventPublisher.publishEvent(
                new ApproveCommitEvent(show.getId(), performer.getId()
                )
        );

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    @Transactional
    public AdminAmateurShowSummaryResponseDTO rejectShow(Long showId, AdminAmateurShowRejectRequestDTO dto) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.reject(dto.getRejectReason());

        Member member  = show.getMember();

        // 등록 거부 커밋 트랜잭션 이벤트 발행
        eventPublisher.publishEvent(
                new RejectCommitEvent(show.getId(), member.getId(), show.getRejectReason()
                )
        );

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    public Slice<AdminApprovalListResponseDTO> getApprovalList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<AmateurShow> pageResult =
                (keyword != null && !keyword.isBlank())
                        ? amateurShowRepository.findByNameContainingIgnoreCase(keyword, pageable)
                        : amateurShowRepository.findAll(pageable);

        List<AdminApprovalListResponseDTO> content = pageResult.getContent().stream()
                .map(this::toApprovalDto)
                .toList();

        return new SliceImpl<>(content, pageable, pageResult.hasNext());
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
