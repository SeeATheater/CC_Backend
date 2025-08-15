package cc.backend.search;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.search.dto.SearchShowResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "검색")
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("")
    @Operation(
            summary = "소극장 공연 검색",
            description = "공연명, 공연진명을 대상으로 키워드 검색하는 기능."
    )
    public ApiResponse<Slice<SearchShowResponseDTO>> searchShows(
            @Parameter(description = "검색 키워드", example = "실종")
            @RequestParam String keyword,

            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ApiResponse.onSuccess(searchService.searchAmateurShows(keyword, pageable));
    }
}
