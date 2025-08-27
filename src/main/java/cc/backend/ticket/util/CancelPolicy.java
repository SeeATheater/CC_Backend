package cc.backend.ticket.util;

import cc.backend.ticket.entity.enums.CancelFeeType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CancelPolicy {

    // 티켓 취소 수수료 유형을 결정
    public static CancelFeeType determineCancelFeeType(LocalDateTime reserveDateTime, LocalDateTime performanceDateTime, LocalDateTime now) {

        LocalDate today = now.toLocalDate(); // 오늘 날짜
        LocalDate reserveDay = reserveDateTime.toLocalDate(); // 예매 날짜
        LocalDate performanceDay = performanceDateTime.toLocalDate(); // 공연 날짜

        // 1. 예매 당일 취소 (밤 12시 이전)
        if (today.isEqual(reserveDay)) {
            return CancelFeeType.NO_FEE; // 수수료 없음
        }

        // 2. 공연일 10일 전까지는 취소 수수료 5000원
        if (today.isBefore(performanceDay.minusDays(10))) {
            return CancelFeeType.FIXED_5000; // 장당 5,000원 수수료
        }

        // 3. 공연일 7~9일 전까지는 취소 수수료 10%
        if (today.isBefore(performanceDay.minusDays(7))) {
            return CancelFeeType.RATE_10; // 티켓 금액의 10% 수수료
        }

        // 4. 공연일 4~6일 전까지는 취소 수수료 20%
        if (today.isBefore(performanceDay.minusDays(4))) {
            return CancelFeeType.RATE_20; // 티켓 금액의 20% 수수료
        }

        // 5. 공연일 1~3일 전까지는 취소 수수료 30%
        if (today.isBefore(performanceDay.minusDays(1))) {
            return CancelFeeType.RATE_30; // 티켓 금액의 30% 수수료
        }

        // 6. 공연일 당일 취소는 환불 불가
        return CancelFeeType.NOT_REFUNDABLE; // 환불 불가
    }

    // 티켓 취소 수수료 계산
    public static int calculateCancelFee(CancelFeeType type, int totalPrice, int quantity) {
        return switch (type) {
            case NO_FEE -> 0; // 수수료 없음
            case FIXED_5000 -> 5000 * quantity; // 장당 5,000원 수수료
            case RATE_10 -> (int) (totalPrice * 0.1); // 티켓 금액의 10% 수수료
            case RATE_20 -> (int) (totalPrice * 0.2); // 티켓 금액의 20% 수수료
            case RATE_30 -> (int) (totalPrice * 0.3); // 티켓 금액의 30% 수수료
            case NOT_REFUNDABLE -> totalPrice; // 환불 불가, 전체 금액을 수수료로 간주
        };
    }

    // 수수료 타입을 설명 문구로 변환
    public static String getCancelFeePolicyText(CancelFeeType type) {
        return switch (type) {
            case NO_FEE -> "예매 당일은 수수료 없이 취소 가능합니다.";
            case FIXED_5000 -> "공연 10일 전까지 취소 시, 티켓당 5,000원의 수수료가 부과됩니다.";
            case RATE_10 -> "공연 7~9일 전 취소 시, 총 금액의 10% 수수료가 부과됩니다.";
            case RATE_20 -> "공연 4~6일 전 취소 시, 총 금액의 20% 수수료가 부과됩니다.";
            case RATE_30 -> "공연 1~3일 전 취소 시, 총 금액의 30% 수수료가 부과됩니다.";
            case NOT_REFUNDABLE -> "공연 당일은 취소 및 환불이 불가능합니다.";
        };
    }
}

