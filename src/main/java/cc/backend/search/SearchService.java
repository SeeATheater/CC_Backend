package cc.backend.search;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
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

    public Slice<SearchShowResponseDTO> searchAmateurShows(String keyword, Pageable pageable) {
        String kw = keyword == null ? "" : keyword.trim();
        Slice<AmateurShow> slice = amateurShowRepository.findByNameOrPerformer(kw, pageable);
        return slice.map(SearchShowResponseDTO::from);
    }
}
