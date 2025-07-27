package cc.backend.amateurShow.dto;
import cc.backend.image.DTO.ImageRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AmateurEnrollRequestDTO {
    private String name; // 공연 이름
    private String place; // 공연장 주소
    private String schedule; // 공연 기간
    private String runtime; // 러닝타임
    private String account; // 계좌번호
    private String contact; // 연락처
    private String hashtag; // 해시태그
    private String summary; // 줄거리
    private Notice notice; // 공지사항 -> 일대일 맵핑
    private List<Casting> casting; // 캐스팅 정보
    private List<Tickets> tickets;
    private List<Staff> staff; // 감독 및 스태프 정보
    private List<Rounds> rounds;

    private ImageRequestDTO.PartialImageRequestDTO imageRequestDTO;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Notice {
        private String content;
        private String noticeImageUrl;
        private String timeInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tickets {
        private String discountName; // COMMON 또는 할인명 입력
        private Integer price;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Casting {
        private String actorName;
        private String castingName;
        private String castingImageUrl; // 캐스팅 이미지 url
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Staff {
        private String position;
        private String staffName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rounds {
        private Integer roundNumber;
        private LocalDateTime performanceDateTime;
        private Integer totalTicket;
    }
}
