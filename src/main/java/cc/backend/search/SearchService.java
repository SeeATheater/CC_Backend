package cc.backend.search;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.search.dto.SearchShowResponseDTO;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final AmateurShowRepository amateurShowRepository;

    public SearchShowResponseDTO.SearchShowDTO.SearchShowResultDTO searchAmateurShows(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();

        // Page로 조회
        Page<AmateurShow> pageResult = amateurShowRepository.findByNameOrPerformer(kw, pageable);

        // DTO 변환
        List<SearchShowResponseDTO.SearchShowDTO> content = pageResult.getContent().stream()
                .map(SearchShowResponseDTO.SearchShowDTO::from)
                .toList();

        // 결과 반환
        return SearchShowResponseDTO.SearchShowDTO.SearchShowResultDTO.builder()
                .searchShowDTOs(content)
                .total(pageResult.getTotalElements()) // total count 포함
                .build();
    }
}
