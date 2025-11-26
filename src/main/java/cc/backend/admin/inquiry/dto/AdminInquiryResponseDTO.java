package cc.backend.admin.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    // 프론트에서 추후 답변 관련 응답 수정 요청시 사용 예정, 요청 없다면 삭제
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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInquiryListResponseDTO {

        @Schema(description = "문의 리스트")
        private List<AdminInquirySummaryResponseDTO> inquiryList;

        @Schema(description = "현재 페이지(0부터 시작)")
        private int page;

        @Schema(description = "페이지 크기")
        private int size;

        @Schema(description = "전체 데이터 수")
        private long totalElements;

        @Schema(description = "전체 페이지 수")
        private int totalPages;

        @Schema(description = "마지막 페이지 여부")
        private boolean last;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminInquirySummaryResponseDTO {

        @Schema(description = "문의 ID")
        private Long inquiryId;

        @Schema(description = "문의 제목")
        private String title;

        @Schema(description = "회원 아이디")
        private String userName;

        @Schema(description = "회원 이메일")
        private String email;

        @Schema(description = "문의 작성일시")
        private LocalDateTime createdAt;

        @Schema(description = "문의 상태")
        private String inquiryStatus;
    }

}
