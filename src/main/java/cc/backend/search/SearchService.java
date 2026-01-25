package cc.backend.search;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.search.dto.SearchShowResponseDTO;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
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

        //Slice로 조회
        Slice<AmateurShow> slice = amateurShowRepository.findByNameOrPerformer(kw, pageable);
        // DTO 변환
        List<SearchShowResponseDTO.SearchShowDTO> content = slice.getContent().stream()
                .map(SearchShowResponseDTO.SearchShowDTO::from)
                .toList();

        // 전체 건수 조회 (Slice로는 content조회, 결과 개수는 별도 count 쿼리)
        long total = amateurShowRepository.countByNameOrPerformer(kw);

        return SearchShowResponseDTO.SearchShowDTO.SearchShowResultDTO.builder()
                .searchShowDTOs(content)
                .total(total)
                .build();
    }
}
