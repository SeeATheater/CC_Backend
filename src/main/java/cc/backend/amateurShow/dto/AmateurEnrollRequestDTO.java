package cc.backend.amateurShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmateurEnrollRequestDTO {
    private String name; // 공연 이름
    private String place; // 공연장 주소
    private String schedule; // 공연 기간
    private String runtime; // 러닝타임
    private String timeInfo; // 공연 시간 정보
    private String account; // 계좌번호
    private String contact; // 연락처
    private String hashtag; // 해시태그
    private String summary; // 줄거리
    private String noticeContent; // 공지사항 -> 일대일 맵핑
    private List<Casting> casting; // 캐스팅 정보
    private List<Tickets> tickets;
    private List<Staff> staff; // 감독 및 스태프 정보
    private List<Rounds> rounds;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tickets {
        private String discountName; // COMMON 또는 할인명 입력
        private Integer price;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Casting {
        private String actorName;
        private String castingName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Staff {
        private String position;
        private String staffName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rounds {
        private int roundNumber;
        private LocalDateTime performanceDateTime;
        private Integer totalTicket;
    }
}
