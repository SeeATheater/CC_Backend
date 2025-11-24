package cc.backend.inquiry.converter;

import cc.backend.inquiry.dto.InquiryRequestDTO;
import cc.backend.inquiry.entity.Inquiry;
import cc.backend.inquiry.entity.InquiryStatus;
import cc.backend.member.entity.Member;

public class InquiryRequestConverter {

    public static Inquiry toEntity(Member member, InquiryRequestDTO.CreateInquiryRequestDTO dto) {
        return Inquiry.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .member(member)
                .inquiryStatus(InquiryStatus.RECEIVED)
                .build();
    }
}
