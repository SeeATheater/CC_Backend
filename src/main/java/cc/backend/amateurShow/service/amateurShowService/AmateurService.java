package cc.backend.amateurShow.service.amateurShowService;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.ticket.dto.response.ReserveListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AmateurService {
    AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Long memberId,
                                                                   AmateurEnrollRequestDTO requestDTO);
    AmateurShowResponseDTO.AmateurShowResult getAmateurShow(Long memberId, Long amateurId);
    AmateurEnrollResponseDTO.AmateurEnrollResult updateShow(Long memberId, Long showId, AmateurUpdateRequestDTO requestDTO);
    void deleteShow(Long memberId, Long amateurShowId);
    List<AmateurShowResponseDTO.AmateurShowList> getShowToday(Long memberId);
    Page<AmateurShowResponseDTO.AmateurShowList> getShowOngoing(Long memberId, Pageable pageable);
    List<AmateurShowResponseDTO.AmateurShowList> getShowRanking(Long memberId);
    List<AmateurShowResponseDTO.AmateurShowList> getIncomingShow(Long memberId);
    List<AmateurShowResponseDTO.AmateurShowList> getShowClosing(Long memberId);


    AmateurShowResponseDTO.MyEnrolledAmateurShowList getMyAmateurShow(Long memberId, AmateurShowStatus showStatus, Pageable pageable);

   // ReserveListResponseDTO getReserveListDetail(Long amateurShowId, Long memberId);
}