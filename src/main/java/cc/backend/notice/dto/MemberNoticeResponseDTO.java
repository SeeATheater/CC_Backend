package cc.backend.notice.dto;

import cc.backend.notice.entity.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

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
        private Long contentId;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MemberNoticeListDTO {
        private Meta meta;
        private List<MemberNoticeDTO> items;

        @Getter
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Meta {
            private int count;
            private boolean empty;
            private boolean hasNext;
            private Long nextCursorId;
            private LocalDateTime nextCursorCreatedAt;
        }

    }
}
