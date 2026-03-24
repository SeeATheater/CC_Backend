package cc.backend.admin.amateurShow.service;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowListResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowReviseRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAmateurShowService {
    private final AmateurShowRepository amateurShowRepository;

    public PageResponse<AdminAmateurShowListResponseDTO> getShowList(int page, int size, String keyword){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        keyword = (keyword == null) ? "" : keyword.trim();
        Page<AmateurShow> pageResult = amateurShowRepository.findByNameContainingIgnoreCase(keyword, pageable);


        Page<AdminAmateurShowListResponseDTO> dtoPage = pageResult.map(this::toListDto);

        return PageResponse.of(dtoPage);
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
