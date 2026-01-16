package cc.backend.apiPayLoad.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import cc.backend.apiPayLoad.code.BaseErrorCode;
import cc.backend.apiPayLoad.code.ReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    //일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다. 로그인 정보를 확인해주세요."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다"),
    _WRONG_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON405", "잘못된 파라미터입니다."),

    //EMAIL ERROR
    EMAIL_FORMAT_ERROR(HttpStatus.NOT_FOUND, "EMAIL4000", "잘못된 이메일 형식입니다."),
    EMAIL_ALREADY_EXIST(HttpStatus.NOT_FOUND, "EMAIL4001", "이미 존재하는 이메일입니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.NOT_FOUND, "EMAIL4002", "인증코드가 만료되었습니다."),
    EMAIL_INVALID_CODE(HttpStatus.NOT_FOUND, "EMAIL4010", "인증코드가 일치하지 않습니다."),


    // MEMBER ERROR
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER4000", "존재하지 않는 사용자입니다."),
    MEMBER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "MEMBER4001", "접근 권한이 없는 사용자 입니다."),
    MEMBER_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "이미 존재하는 사용자(이메일) 입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "MEMBER4003", "비밀번호가 일치하지 않습니다."),
    MEMBER_INVALID_CODE(HttpStatus.BAD_REQUEST, "MEMBER4004", "토큰이 유효하지 않습니다."),
    MEMBER_NOT_ADMIN(HttpStatus.FORBIDDEN, "MEMBER4005", "해당 사용자게에게 관리자 권한이 없습니다."),
    MEMBER_NOT_PERFORMER(HttpStatus.FORBIDDEN, "MEMBER4011", "등록자 계정이 아니기에 접근권한이 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "MEMBER4012", "유효하지않은 리프레시 토큰입니다."),
    INVALID_USERNAME_EMPTY(HttpStatus.BAD_REQUEST, "MEMBER4013", "유저네임 입력값이 비어있습니다."),
    INVALID_USERNAME_LENGTH(HttpStatus.BAD_REQUEST, "MEMBER4014", "유저네임은 1~20자로 설정해주세요 "),
    PHONENUM_ENCRYPT_FAIL(HttpStatus.BAD_REQUEST, "MEMBER4015", "전화번호 암호화 실패"),
    MEMBER_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "MEMBER4016", "해당 회원은 이미 탈퇴(비활성화) 상태입니다,"),
    MEMBER_ALREADY_ACTIVATED(HttpStatus.BAD_REQUEST, "MEMBER4017", "해당 회원은 이미 활성화 상태입니다,"),


    //KAKAO
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "KAKAO4001", "카카오 토큰 요청에 실패했습니다."),
    KAKAO_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "KAKAO4002", "카카오 사용자 정보 요청에 실패했습니다."),
    INVALID_KAKAO_USER_INFO(HttpStatus.BAD_REQUEST, "KAKAO4003", "카카오 사용자 정보가 유효하지 않습니다."),
    MEMBER_ROLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "KAKAO4004", "이미 다른 역할로 가입된 계정입니다."),


    // TEMP TICKET ERROR
    TEMP_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "TICKET4000", "존재하지 않는 예약 티켓입니다."),
    TEMP_TICKET_QUANTITY(HttpStatus.BAD_REQUEST, "TICKET4001", "주문 수량이 적절하지 않습니다"),
    TEMP_TICKET_STOCK(HttpStatus.BAD_REQUEST, "TICKET4002", "주문 수량이 재고를 초과했습니다"),
    TEMP_TICKET_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "TICKET4003", "이미 취소하신 티켓입니다."),
    TEMP_TICKET_WRONG_STATUS(HttpStatus.BAD_REQUEST, "TICKET4004", "티켓의 상태가 적절하지 않습니다."),
    TEMP_TICKET_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "TICKET4005", "이미 예매된 티켓입니다."),
    TEMP_TICKET_TID_NOT_FOUND(HttpStatus.NOT_FOUND, "TICKET4006", "존재하지 않는 결제 고유 번호(TID) 입니다."),
    TEMP_TICKET_STATUS_INVALID(HttpStatus.BAD_REQUEST, "TICKET4007", "유효하지 않은 티켓 상태입니다."),
    TEMP_TICKET_EXPIRED(HttpStatus.BAD_REQUEST, "TICKET4008", "티켓이 만료되었습니다."),
    NOT_TEMP_TICKET_OWNER(HttpStatus.FORBIDDEN, "TICKET4009", "해당 티켓의 소유자가 아닙니다."),


    // REAL TICKET ERROR
    REAL_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "REALTICKET4000", "존재하지 않는 실제 티켓입니다."),
    REAL_TICKET_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "REALTICKET4001", "이미 취소하신 티켓입니다."),
    INVALID_RESERVATION_STATUS(HttpStatus.BAD_REQUEST, "REALTICKET4002", "지원하지 않는 티켓 타입입니다."),
    REAL_TICKET_CANCEL_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "REALTICKET4004", "티켓 취소 가능 기한이 이미 지났습니다."),
    NOT_REAL_TICKET_OWNER(HttpStatus.FORBIDDEN, "REALTICKET4005", "해당 실제 티켓의 소유자가 아닙니다."),

    // S3 ERROR
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE4000", "이미지를 찾을 수 없습니다."),
    NOT_FOUND_IN_S3(HttpStatus.NOT_FOUND, "S34000", "해당 이미지가 S3 내에 존재하지 않습니다."),
    INVALID_S3_KEY(HttpStatus.BAD_REQUEST, "S34001", "올바르지 않은 key 입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "S34002", "올바르지 않은 파일입니다."),

    //REPORT ERROR
    UNSUPPORTED_OBJECT_TYPE(HttpStatus.BAD_REQUEST, "REPORT4000", "지원하지 않는 신고 타입입니다."),

    // AMATEURSHOW ERROR
    AMATEURSHOW_NOT_FOUND(HttpStatus.NOT_FOUND, "AMATEURSHOW4000", "존재하지 않는 소극장 공연입니다."),
    INVALID_DATE_RANGE(HttpStatus.NOT_ACCEPTABLE, "AMATEURSHOW4001", "공연 시작 날짜는 종료 날짜 이전이어햐 합니다."),

    // AMATEUR TICKET ERROR
    AMATEUR_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "AMATEURTICKET4000", "존재하지 않는 소극장 공연 티켓입니다."),
    AMATEUR_TICKET_STOCK(HttpStatus.BAD_REQUEST, "AMATEURTICKET4001", "주문 수량은 최소 1개 이상이어야 합니다."),
    AMATEUR_SHOW_MISMATCH(HttpStatus.NOT_FOUND, "AMATEURTICKET4002", "회차와 티켓에 해당하는 공연이 일치하지 않습니다."),

    // PHOTOALBUM ERROR
    PHOTOALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTOALBUM4000", "존재하지 않는 사진첩입니다."),

    // ROUND ERROR
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUND4000", "존재하지 않는 회차입니다."),
    ROUND_BOOKING_DEADLINE_PASSED(HttpStatus.FORBIDDEN, "ROUND4001", "예약 가능한 회차가 아닙니다."),

    //BOARD ERROR
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD4001", "게시글이 존재하지 않습니다."),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BOARD4002", "수정/삭제 권한이 없습니다."),
    ONLY_PERFORMER_CAN_WRITE_PROMOTION(HttpStatus.FORBIDDEN, "BOARD4003", "홍보게시판은 Performer만 작성할 수 있습니다."),
    INVALID_BOARD_TYPE(HttpStatus.BAD_REQUEST, "BOARD4004", "유효하지 않은 게시판 타입입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT4001", "댓글이 존재하지 않습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT4002", "댓글 수정/삭제 권한이 없습니다."),
    COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT4003", "대댓글의 depth는 1까지만 허용됩니다."),
    COMMENT_BOARD_MISMATCH(HttpStatus.BAD_REQUEST, "COMMENT4004", "해당 댓글은 이 게시글에 속하지 않습니다."),

    // REPORT ERROR
    ALREADY_REPORTED(HttpStatus.BAD_REQUEST, "REPORT4001", "이미 신고한 게시글입니다."),
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REPORT4002", "본인이 작성한 글/댓글은 신고할 수 없습니다."),

    //NOTICE ERROR
    MEMBERNOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBERNOTICE4001", "존재하지 않는 알림입니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE4001", "존재하지 않는 알림입니다."),
    // INQUIRY ERROR
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY4000", "존재하지 않는 문의글입니다."),
    FORBIDDEN_INQUIRY_ACCESS(HttpStatus.NOT_FOUND, "INQUIRY4001", "로그인한 멤버가 작성하지 않는 문의글입니다."),

    // MEMBER LIKE ERROR
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE4001", "존재하지 않는 좋아요입니다."),
    DUPLICATE_LIKE(HttpStatus.BAD_REQUEST, "LIKE4002", "이미 좋아요한 공연진입니다."),


    // ADMIN ERROR
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN4001", "관리자 계정이 없습니다");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    //Error
    @Override
    public ReasonDTO getErrorReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ReasonDTO getErrorReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }

}