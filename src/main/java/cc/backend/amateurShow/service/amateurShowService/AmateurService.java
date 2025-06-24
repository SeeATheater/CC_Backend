package cc.backend.amateurShow.service.amateurShowService;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AmateurService {
    AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Long memberId,
                                                                   AmateurEnrollRequestDTO requestDTO);
    AmateurShowResponseDTO.AmateurShowResult getAmateurShow(Long amateurId);
    AmateurEnrollResponseDTO.AmateurEnrollResult updateShow(Long showId, AmateurUpdateRequestDTO requestDTO);
    void deleteShow(Long amateurShowId);
    List<AmateurShowResponseDTO.AmateurShowToday> getShowToday();
}