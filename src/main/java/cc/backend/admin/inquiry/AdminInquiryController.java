package cc.backend.admin.inquiry;

import cc.backend.admin.inquiry.dto.AdminInquiryRequestDTO;
import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.SliceResponse;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inquiry")
@RequiredArgsConstructor
@Tag(name = "관리자 문의 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {
    private final AdminInquiryService adminInquiryService;

    @Operation(summary = "관리자 문의 단건 조회 API",description = "관리자가 문의를 단건 조회하는 기능입니다.")
    @GetMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO> getInquiryDetails(
            @PathVariable Long inquiryId) {
        return ApiResponse.onSuccess(adminInquiryService.getInquiryDetails(inquiryId));
    }

    @Operation(summary = "관리자 문의 답변 달기/답변 수정하기 API",
            description = "관리자가 문의에 답글을 생성한는 기능입니다. 초기 답글 달기, 수정하기 같은 요청입니다.")
    @PatchMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO> replyInquiry(@PathVariable Long inquiryId,
                                          @RequestBody AdminInquiryRequestDTO.Reply requestDTO) {
        return ApiResponse.onSuccess(adminInquiryService.replyInquiry(inquiryId, requestDTO));
    }

    @Operation(summary = "관리자 문의 리스트/검색 API",
            description = "관리자가 문의 리스트를 합니다. keyword로 제목/내용 검색을 합니다.")
    @GetMapping("")
    public ApiResponse<SliceResponse<AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO>> getInquiryList(
            @Parameter(description = "검색 키워드(제목/내용)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO> slice =
                adminInquiryService.getInquiryList(keyword, pageable);

        return ApiResponse.onSuccess(SliceResponse.of(slice));
    }

}
