package cc.backend.amateurShow.entity;

public enum AmateurShowStatus {
    YET, // 지금 날짜가 공연 날짜 안에 없을 때 (아직 공연 예정)
    ONGOING, // 지금 날짜가 공연 날짜 안에 있을 때 (예매 진행 중)
    ENDED;    // 지금 날짜가 공연 날짜보다 지났을 때 (공연 종료됨)
}
