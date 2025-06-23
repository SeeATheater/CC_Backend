package cc.backend.notice.dto;

import cc.backend.notice.entity.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NoticeResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeDTO {

        private Long id;
        private NoticeType noticeType;
        private String message;
        private Long contentId;
        private LocalDateTime createdAt;

    }
}
