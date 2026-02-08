package cc.backend.amateurShow.service.amateurShowService;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AmateurService {
    AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Long memberId,
                                                                   AmateurEnrollRequestDTO requestDTO);
    AmateurShowResponseDTO.AmateurShowResult getAmateurShow(Long amateurId);
    AmateurEnrollResponseDTO.AmateurEnrollResult updateShow(Long memberId, Long showId, AmateurUpdateRequestDTO requestDTO);
    void deleteShow(Long memberId, Long amateurShowId);
    Slice<AmateurShowResponseDTO.AmateurShowList> getShowToday(Pageable pageable);
    Slice<AmateurShowResponseDTO.AmateurShowList> getShowOngoing(Pageable pageable);
    List<AmateurShowResponseDTO.AmateurShowList> getShowRanking();
    List<AmateurShowResponseDTO.AmateurShowList> getRecentlyHotShow();
    List<AmateurShowResponseDTO.AmateurShowList> getShowClosing();
    AmateurShowResponseDTO.AmateurShowResult getCreatedShow(Long memberId, Long amateurId);


    Slice<AmateurShowResponseDTO.MyShowAmateurShowList> getMyAmateurShow(Long memberId, AmateurShowStatus showStatus, Pageable pageable);

   // ReserveListResponseDTO getReserveListDetail(Long amateurShowId, Long memberId);
}