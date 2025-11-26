package cc.backend.admin.inquiry.converter;

import cc.backend.admin.inquiry.dto.AdminInquiryResponseDTO;
import cc.backend.inquiry.entity.Inquiry;
import cc.backend.member.entity.Member;

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
}
