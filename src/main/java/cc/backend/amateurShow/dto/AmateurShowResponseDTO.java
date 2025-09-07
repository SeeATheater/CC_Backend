package cc.backend.amateurShow.dto;

import cc.backend.amateurShow.entity.AmateurShowStatus;
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
        private Long memberId; //등록자 memberId
        private Long amateurShowId; // 소극장 공연 id
        private String name; // 공연 이름
        private String performerName; // 공연진 이름
        private String hallName; // 공연장 이름
        //private String place; // 공연장 주소
        private String roadAddress; // 공연장 도로명 주소
        private String detailAddress; // 공연장 상세 주소
        private String schedule; // 공연 기간
        private String runtime; // 러닝타임
        private String bankName; // 은행명
        private String account; // 계좌번호
        private String depositor; // 입금자명
        private String contact; // 연락처
        private String hashtag; // 해시태그
        private String summary; // 줄거리
        private String posterImageUrl;
        private AmateurShowResult.Notice notice;
        private List<AmateurShowResult.Casting> casting; // 캐스팅 정보
        private List<AmateurShowResult.Staff> staff; // 감독 및 스태프 정보
        private List<AmateurShowResult.Rounds> rounds; // 회차 정보
        private List<AmateurShowResult.Tickets> tickets;// 티켓 정보

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Notice {
            private Long noticeId;
            private String content;
            private String noticeImageUrl;
            private String timeInfo;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Tickets {
            private Long ticketId;
            private String discountName;
            private Integer price;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Casting {
            private Long castingId;
            private String actorName;
            private String castingName;
            private String castingImageUrl;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Staff {
            private Long staffId;
            private String position;
            private String staffName;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Rounds {
            private Long roundId;
            private Integer roundNumber;
            private LocalDateTime performanceDateTime;
            private Integer totalTicket;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AmateurShowList { // 극장 공연 리스트 조회
        private Long amateurShowId; // 소극장 공연 id
        private String name; // 공연 이름
        //private String place; // 공연장 주소
        private String detailAddress; // 공연장 상세 주소
        private String schedule;
        private String posterImageUrl;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MyShowAmateurShowList { // 극장 공연 리스트 조회
        private Long amateurShowId; // 소극장 공연 id
        private String name; // 공연 이름
        //private String place; // 공연장 주소
        private String detailAddress; // 공연장 상세 주소
        private String schedule;
        private String posterImageUrl;
        private AmateurShowStatus status;
    }
}
