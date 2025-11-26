package cc.backend.admin.inquiry;

import cc.backend.admin.inquiry.dto.AdminInquiryRequestDTO;
import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.apiPayLoad.ApiResponse;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/inquiry")
@RequiredArgsConstructor
@Tag(name = "관리자 문의 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {
    private final AdminInquiryService adminInquiryService;

    @Operation(summary = "관리자 문의 단건 조회",description = "관리자가 문의를 단건 조회하는 API")
    @GetMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO> getInquiryDetails(
            @PathVariable Long inquiryId) {
        return ApiResponse.onSuccess(adminInquiryService.getInquiryDetails(inquiryId));
    }

    @Operation(summary = "관리자 문의 답변 달기", description = "관리자가 문의에 답글을 다는 API")
    @PatchMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO> replyInquiry(@PathVariable Long inquiryId,
                                          @RequestBody AdminInquiryRequestDTO.Reply requestDTO) {
        return ApiResponse.onSuccess(adminInquiryService.replyInquiry(inquiryId, requestDTO));
    }
}
