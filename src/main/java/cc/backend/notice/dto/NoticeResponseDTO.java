package cc.backend.notice.dto;

import cc.backend.notice.entity.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class NoticeResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardNoticeResultDTO {

        private NoticeType noticeType;
        private Long id;
        private String title;
        private String content;

    }
}
