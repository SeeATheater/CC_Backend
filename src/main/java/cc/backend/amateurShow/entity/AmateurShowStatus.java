package cc.backend.amateurShow.entity;

public enum AmateurShowStatus {
    WAITING_APPROVAL, // 공연 등록만 하고, 관리자가 승인을 안 했을 때
    APPROVED_YET, // 관리자가 승인을 하고, 지금 날짜가 공연 날짜 안에 없을 때 (아직 공연 예정)
    APPROVED_ONGOING, // 관리자가 승인을 하고, 지금 날짜가 공연 날짜 안에 있을 때 (예매 진행 중)
    APPROVED_ENDED    // 관리자가 승인을 하고, 지금 날짜가 공연 날짜보다 지났을 때 (공연 종료됨)
}
