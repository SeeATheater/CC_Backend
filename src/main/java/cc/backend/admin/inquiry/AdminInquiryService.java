package cc.backend.admin.inquiry;

import cc.backend.admin.inquiry.converter.AdminInquiryResponseConverter;
import cc.backend.admin.inquiry.dto.AdminInquiryRequestDTO;
import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.inquiry.entity.Inquiry;
import cc.backend.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {
    private final InquiryRepository inquiryRepository;

    public AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO getInquiryDetails(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(()-> new GeneralException(ErrorStatus.INQUIRY_NOT_FOUND));
        return AdminInquiryResponseConverter.toInquiryDetailDTO(inquiry);
    }

    @Transactional
    public AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO replyInquiry(Long inquiryId, AdminInquiryRequestDTO.Reply requestDTO) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(()-> new GeneralException(ErrorStatus.INQUIRY_NOT_FOUND));
        inquiry.updateReply(requestDTO.getReplyContent());
        return AdminInquiryResponseConverter.toInquiryDetailDTO(inquiry);
    }

    public Page<AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO> getInquiryList(String keyword,
                                                                                     Pageable pageable) {
        Page<Inquiry> inquiryPage;
        if (StringUtils.hasText(keyword)) {
            // 제목 or 내용
            inquiryPage = inquiryRepository
                    .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            // 없으면
            inquiryPage = inquiryRepository.findAll(pageable);
        }
        List<AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO> content = inquiryPage.getContent().stream()
                .map(AdminInquiryResponseConverter::toSummaryDTO)
                .toList();
        return new PageImpl<>(content, pageable, inquiryPage.getTotalElements());
    }
}
