package cc.backend.ticket.entity.enums;

public enum CancelFeeType {
    NO_FEE, // 수수료 없음
    FIXED_5000, // 장당 5,000원 수수료
    RATE_10, // 티켓 금액의 10% 수수료
    RATE_20, // 티켓 금액의 20% 수수료
    RATE_30, // 티켓 금액의 30% 수수료
    NOT_REFUNDABLE; // 환불 불가
}
