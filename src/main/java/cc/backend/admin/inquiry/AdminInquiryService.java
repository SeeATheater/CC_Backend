package cc.backend.admin.inquiry;

import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {
    private final InquiryRepository inquiryRepository;

    public AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO getInquiryDetails(Long inquiryId) {
        return null;
    }
}
