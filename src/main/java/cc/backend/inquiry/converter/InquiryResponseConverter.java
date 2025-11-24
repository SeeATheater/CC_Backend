package cc.backend.inquiry.converter;

import cc.backend.inquiry.dto.InquiryResponseDTO;
import cc.backend.inquiry.entity.Inquiry;

public class InquiryResponseConverter {

    public static InquiryResponseDTO.CreateInquiryResponseDTO toDTO(Inquiry inquiry) {
        return InquiryResponseDTO.CreateInquiryResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .createTime(inquiry.getCreatedAt())
                .build();
    }

    public static InquiryResponseDTO.InquiryDetailResponseDTO toDetailDTO(Inquiry inquiry) {
        return InquiryResponseDTO.InquiryDetailResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .createTime(inquiry.getCreatedAt())
                .inquiryTitle(inquiry.getTitle())
                .inquiryContent(inquiry.getContent())
                .inquiryStatus(inquiry.getInquiryStatus().toString())
                .repliedAt(inquiry.getRepliedAt())
                .inquiryReply(inquiry.getReply())
                .inquiryMemberName(inquiry.getMember().getName()).build();
    }
}
