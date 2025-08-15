package cc.backend.performer;


import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.performer.dto.PerformerMyShowResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformerService {
    private final AmateurShowRepository amateurShowRepository;

    public Slice<PerformerMyShowResponseDTO> getMyShows(Long memberId, String tab, Pageable pageable) {

        Slice<AmateurShow> slice;

        if ("on_sale".equalsIgnoreCase(tab)) { // 예매 진행
            slice = amateurShowRepository.findByMember_IdAndStatusInOrderByIdDesc(
                    memberId,
                    EnumSet.of(AmateurShowStatus.APPROVED_ONGOING, AmateurShowStatus.APPROVED_YET),
                    pageable
            );
        } else if ("ended".equalsIgnoreCase(tab)) { // 공연 종료
            slice = amateurShowRepository.findByMember_IdAndStatusInOrderByIdDesc(
                    memberId,
                    EnumSet.of(AmateurShowStatus.APPROVED_ENDED),
                    pageable
            );
        } else { // 전체
            slice = amateurShowRepository.findByMember_IdOrderByIdDesc(memberId, pageable);
        }

        return slice.map(PerformerMyShowResponseDTO::from);
    }
}
