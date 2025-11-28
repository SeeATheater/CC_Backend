package cc.backend.inquiry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class InquiryRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInquiryRequestDTO {
        @Schema(description = "문의 제목", example = "티켓 관련 문의합니다.")
        @NotBlank(message = "제목은 필수 입력 값입니다.")
        private String title;
        @Schema(description = "문의 내용", example = "문의 내용입니다.")
        @NotBlank(message = "내용은 필수 입력 값입니다.")
        private String content;

    }
}
