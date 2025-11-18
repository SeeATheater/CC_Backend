package cc.backend.amateurShow.dto;

import cc.backend.image.DTO.ImageRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AmateurUpdateRequestDTO {
    private String name; // 공연 이름
    private String performerName; // 공연진 이름
    private String hallName; // 공연장 이름
    private String roadAddress; // 공연장 도로명 주소
    private String detailAddress; // 공연장 상세 주소
    //private String place; // 공연장 주소
    private LocalDate start; // 공연 시작 날짜
    private LocalDate end; // 공연 종료 날짜
    private String runtime; // 러닝타임
    private String bankName; // 은행명
    private String account; // 계좌번호
    private String depositor; // 입금자명
    private String contact; // 연락처
    private String hashtag; // 해시태그
    private String summary; // 줄거리
    private ImageRequestDTO.PosterImageRequestDTO posterImageRequestDTO; // 포스터 이미지
    private AmateurUpdateRequestDTO.UpdateNotice notice; // 공지사항 -> 일대일 맵핑
    private List<UpdateCasting> casting; // 캐스팅 정보
    private List<UpdateTickets> tickets;
    private List<UpdateStaff> staff; // 감독 및 스태프 정보
    private List<UpdateRounds> rounds;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateNotice {
        private Long noticeId;
        private String content;
        private ImageRequestDTO.NoticeImageRequestDTO noticeImageRequestDTO;
        private String timeInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateTickets {
        private Long ticketId;
        private String discountName; // COMMON 또는 할인명 입력
        private Integer price;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCasting {
        private Long castingId;
        private String actorName;
        private String castingName;
        private ImageRequestDTO.CastingImageRequestDTO castingImageRequestDTO; // 캐스팅 이미지
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStaff {
        private Long staffId;
        private String position;
        private String staffName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRounds {
        private Long roundId;
        private Integer roundNumber;
        private LocalDateTime performanceDateTime;
        private Integer totalTicket;
    }
}
