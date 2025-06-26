package cc.backend.amateurShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AmateurEnrollResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmateurEnrollResult{
        private Long amateurShowId; // 소극장 공연 id
        private String name; // 공연 이름
    }
}
