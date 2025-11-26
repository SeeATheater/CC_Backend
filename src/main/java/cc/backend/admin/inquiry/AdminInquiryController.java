package cc.backend.admin.inquiry;

import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.apiPayLoad.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/inquiry")
@RequiredArgsConstructor
@Tag(name = "관리자 문의 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInquiryController {
    private final AdminInquiryService adminInquiryService;

    @GetMapping("/{inquiryId}")
    public ApiResponse<AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO> getInquiryDetails(@PathVariable Long inquiryId) {
        return null;
    }
}
