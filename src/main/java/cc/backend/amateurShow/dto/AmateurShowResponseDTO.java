package cc.backend.amateurShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class AmateurShowResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmateurShowResult{ // 소극장 공연 단건 조회용
        private Long amateurShowId; // 소극장 공연 id
        private String name; // 공연 이름
        private String troupe; // 공연진 이름
        private String place; // 공연장 주소
        private String schedule; // 공연 기간
        private String runtime; // 러닝타임
        private String age; // 관람 연령
        private String starring; // 출연 정보
        private int totalTicket; // 총 티켓 수
        private String timeInfo; // 공연 시간 정보
        private List<AmateurShowResult.RegularTicket> regularTicket; // 일반 예매 가격 (여러개 추가 가능)
        private String account; // 계좌번호
        private String contact; // 연락처
        private String hashtag; // 해시태그
        private String summaryContent; // 줄거리 -> 일대일 맵핑
        private String noticeContent; // 공지사항 -> 일대일 맵핑
        private List<AmateurShowResult.Casting> casting; // 캐스팅 정보
        private List<AmateurShowResult.DiscountTicket> discountTicket; // 할인 티켓 정보
        private List<AmateurShowResult.Staff> staff; // 감독 및 스태프 정보
        private List<AmateurShowResult.Rounds> rounds; // 회차 정보

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
        public static class DiscountTicket {
            private String discountName;
            private Integer discountPrice;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RegularTicket {
            private Integer regularPrice;
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
            private Integer roundNumber;
            private LocalDateTime performanceDateTime;
        }
    }
}
