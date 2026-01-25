package cc.backend.search;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.specification.AmateurShowSpecification;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.search.dto.SearchShowResponseDTO;
import io.micrometer.core.instrument.search.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final AmateurShowRepository amateurShowRepository;

    public SearchShowResponseDTO.SearchShowDTO.SearchShowResultDTO searchAmateurShows(String keyword, int page, int size) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<AmateurShow> spec = Specification
                .where(AmateurShowSpecification.nameOrPerformerContains(kw))
                .and(AmateurShowSpecification.isApproved());

        // Page로 조회
        Page<AmateurShow> pageResult = amateurShowRepository.findAll(spec, pageable);

        // DTO 변환
        Page<SearchShowResponseDTO.SearchShowDTO> dtoPage = pageResult.map(SearchShowResponseDTO.SearchShowDTO::from);

        // 결과 반환
        return SearchShowResponseDTO.SearchShowDTO.SearchShowResultDTO.builder()
                .searchShowDTOs(dtoPage.getContent())
                .total(pageResult.getTotalElements()) // total count 포함
                .build();
    }
}
