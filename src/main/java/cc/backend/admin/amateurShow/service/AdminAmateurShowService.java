package cc.backend.admin.amateurShow.service;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowListResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowReviseRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.SliceResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.event.entity.ApproveShowEvent;
import cc.backend.event.entity.NewShowEvent;
import cc.backend.event.entity.RejectShowEvent;
import cc.backend.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static io.micrometer.common.util.StringUtils.isNotBlank;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAmateurShowService {
     private final AmateurShowRepository amateurShowRepository;
     private final ApplicationEventPublisher eventPublisher;

    public Page<AdminAmateurShowListResponseDTO> getShowList(int page, int size, String keyword){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<AmateurShow> pageResult;
        if (keyword != null && !keyword.isBlank()) {
            pageResult = amateurShowRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            pageResult = amateurShowRepository.findAll(pageable);
        }

        return pageResult.map(this::toListDto);
    }

    private AdminAmateurShowListResponseDTO toListDto(AmateurShow show){
        return AdminAmateurShowListResponseDTO.builder()
                .showId(show.getId())
                .showName(show.getName())
                .createdAt(show.getCreatedAt())
                .performerName(show.getPerformerName())
                .amateurShowStatus(show.getStatus().toString()).build();
    }

    public AdminAmateurShowSummaryResponseDTO getShowSummary(Long showId) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    @Transactional
    public AdminAmateurShowSummaryResponseDTO reviseShow(Long showId, AdminAmateurShowReviseRequestDTO dto) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.reviseShowInfo(dto.getHashtag(), dto.getSummary(), dto.getAccount(), dto.getContact());


        return AdminAmateurShowSummaryResponseDTO.from(show);
    }




}
