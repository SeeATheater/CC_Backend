package cc.backend.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class InquiryResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInquiryResponseDTO {
        @Schema(description = "문의 id")
        private Long inquiryId;
        @Schema(description = "문의 등록일")
        private LocalDateTime createTime;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  InquiryDetailResponseDTO {
        @Schema(description = "문의 id")
        private Long inquiryId;

        @Schema(description = "문의 등록 날짜")
        private LocalDateTime createTime;

        @Schema(description = "문의 제목")
        private String inquiryTitle;

        @Schema(description = "문의 내용")
        private String inquiryContent;

        @Schema(description = "문의 상태")
        private String inquiryStatus;

        @Schema(description = "문의 답변 날짜")
        private LocalDateTime repliedAt;

        @Schema(description = "문의 답변 내용")
        private String inquiryReply;

        @Schema(description = "등록자 이름")
        private String inquiryMemberName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InquiryListResponseDTO {
        private List<InquirySummaryResponseDTO> inquiryList;
        private int page;
        private int size;
        private boolean hasNext;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  InquirySummaryResponseDTO {
        @Schema(description = "문의 id")
        private Long inquiryId;

        @Schema(description = "문의 등록일")
        private LocalDateTime createTime;

        @Schema(description = "문의 제목")
        private String inquiryTitle;

        @Schema(description = "문의 상태")
        private String inquiryStatus;

    }


}
