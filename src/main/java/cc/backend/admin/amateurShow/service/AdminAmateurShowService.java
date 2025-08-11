package cc.backend.admin.amateurShow.service;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowListResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowReviseRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import lombok.RequiredArgsConstructor;
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

//    public ApiResponse<List<AdminAmateurShowListResponseDTO>> getShowList(){
//
//    }

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

    @Transactional
    public AdminAmateurShowSummaryResponseDTO approveShow(Long showId) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.approve();
        return AdminAmateurShowSummaryResponseDTO.from(show);
    }

    @Transactional
    public AdminAmateurShowSummaryResponseDTO rejectShow(Long showId, AdminAmateurShowRejectRequestDTO dto) {
        AmateurShow show = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        show.reject(dto.getRejectReason());
        return AdminAmateurShowSummaryResponseDTO.from(show);
    }


}
