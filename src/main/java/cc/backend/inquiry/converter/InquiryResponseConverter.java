package cc.backend.inquiry.converter;

import cc.backend.inquiry.dto.InquiryResponseDTO;
import cc.backend.inquiry.entity.Inquiry;
import org.springframework.data.domain.Slice;

import java.util.List;

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

    public static InquiryResponseDTO.InquirySummaryResponseDTO toSummaryDTO(Inquiry inquiry) {
        return InquiryResponseDTO.InquirySummaryResponseDTO.builder()
                .inquiryId(inquiry.getId())
                .createTime(inquiry.getCreatedAt())
                .inquiryTitle(inquiry.getTitle())
                .inquiryStatus(inquiry.getInquiryStatus().name())
                .build();
    }

    public static InquiryResponseDTO.InquiryListResponseDTO toInquirySliceDTO(Slice<Inquiry> slice) {

        List<InquiryResponseDTO.InquirySummaryResponseDTO> list =
                slice.getContent().stream()
                        .map(InquiryResponseConverter::toSummaryDTO)
                        .toList();

        return InquiryResponseDTO.InquiryListResponseDTO.builder()
                .inquiryList(list)
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }
}
