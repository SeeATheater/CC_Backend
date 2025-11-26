package cc.backend.admin.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

public class AdminInquiryResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInquiryDetailResponseDTO {

        @Schema(description = "문의 ID")
        private Long inquiryId;

        @Schema(description = "회원 이름")
        private String memberName;

        @Schema(description = "회원 전화번호")
        private String memberPhoneNumber;

        @Schema(description = "회원 이메일")
        private String memberEmail;

        @Schema(description = "문의 제목")
        private String title;

        @Schema(description = "문의 내용")
        private String content;

        @Schema(description = "문의 상태")
        private String inquiryStatus;

        @Schema(description = "문의 작성일시")
        private LocalDateTime createdAt;

        @Schema(description = "답변 내용", nullable = true)
        private String reply;

        @Schema(description = "답변 작성일시", nullable = true)
        private LocalDateTime repliedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInquiryReplyResponseDTO {

        @Schema(description = "문의 ID")
        private Long inquiryId;

        @Schema(description = "문의 상태")
        private String inquiryStatus;

        @Schema(description = "답변 내용", nullable = true)
        private String reply;

        @Schema(description = "답변 작성일시", nullable = true)
        private LocalDateTime repliedAt;
    }

}
