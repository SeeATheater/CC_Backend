package cc.backend.notice.dto;

import cc.backend.notice.entity.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MemberNoticeResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberNoticeDTO {

        private Long id;
        private NoticeType noticeType;
        private String message;
        private Boolean isread;
        private LocalDateTime createdAt;

    }
}
