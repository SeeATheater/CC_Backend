package cc.backend.amateurShow.dto;
import cc.backend.amateurShow.validator.ValidScheduleDate;
import cc.backend.image.DTO.ImageRequestDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ValidScheduleDate
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AmateurEnrollRequestDTO {

    private String name; // 공연 이름
    private String hallName; // 공연장 이름
    private String roadAddress; // 공연장 도로명 주소
    private String detailAddress; // 공연장 상세 주소

    @NotNull(message = "시작 날짜는 필수입니다")
    private LocalDate start; // 공연 시작 날짜

    @NotNull(message = "종료 날짜는 필수입니다")
    private LocalDate end; // 공연 종료 날짜

    private String runtime; // 러닝타임
    private String bankName; // 은행명
    private String account; // 계좌번호
    private String depositor; // 입금자명
    private String contact; // 연락처
    private String hashtag; // 해시태그
    private String summary; // 줄거리
    private Notice notice; // 공지사항 -> 일대일 맵핑
    private List<Casting> casting; // 캐스팅 정보
    private List<Tickets> tickets;
    private List<Staff> staff; // 감독 및 스태프 정보
    private List<Rounds> rounds;

    private ImageRequestDTO.PartialImageRequestDTO posterImageRequestDTO;

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
