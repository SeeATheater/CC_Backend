package cc.backend.admin.inquiry.converter;

import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.inquiry.entity.Inquiry;
import cc.backend.member.entity.Member;
import org.springframework.data.domain.Page;

import java.util.List;

public class AdminInquiryResponseConverter {

    public static AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO toInquiryDetailDTO(Inquiry inquiry) {

        Member member = inquiry.getMember();

        return AdminInquiryResponseDTO.AdminInquiryDetailResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .memberName(member.getName())
                .memberPhoneNumber(member.getPhone())
                .memberEmail(member.getEmail())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .inquiryStatus(inquiry.getInquiryStatus().name())
                .createdAt(inquiry.getCreatedAt())
                .reply(inquiry.getReply())
                .repliedAt(inquiry.getRepliedAt())
                .build();
    }

    public static AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO toSummaryDTO(Inquiry inquiry) {
        Member member = inquiry.getMember();

        return AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .title(inquiry.getTitle())
                .userName(member.getUsername())
                .email(member.getEmail())
                .createdAt(inquiry.getCreatedAt())
                .inquiryStatus(inquiry.getInquiryStatus().name())
                .build();
    }

    public static AdminInquiryResponseDTO.AdminInquiryListResponseDTO toListDTO(Page<Inquiry> inquiryPage) {

        List<AdminInquiryResponseDTO.AdminInquirySummaryResponseDTO> list =
                inquiryPage.getContent().stream()
                        .map(AdminInquiryResponseConverter::toSummaryDTO)
                        .toList();

        return AdminInquiryResponseDTO.AdminInquiryListResponseDTO.builder()
                .inquiryList(list)
                .page(inquiryPage.getNumber())
                .size(inquiryPage.getSize())
                .totalElements(inquiryPage.getTotalElements())
                .totalPages(inquiryPage.getTotalPages())
                .last(inquiryPage.isLast())
                .build();
    }
}
