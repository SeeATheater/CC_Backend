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
    MEMBER_INVALID_CODE(HttpStatus.BAD_REQUEST, "MEMBER4010", "토큰이 유효하지 않습니다."),
    MEMBER_NOT_ADMIN(HttpStatus.FORBIDDEN, "MEMBER4011", "해당 사용자게에게 관리자 권한이 없습니다."),
    MEMBER_ALREADY_DEACTIVATED(HttpStatus.BAD_REQUEST, "MEMBER4012", "해당 회원은 이미 탈퇴(비활성화) 상태입니다,"),
    MEMBER_ALREADY_ACTIVATED(HttpStatus.BAD_REQUEST, "MEMBER4012", "해당 회원은 이미 활성화 상태입니다,"),

    // MEMBER TICKET ERROR
    MEMBER_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "TICKET4000", "존재하지 않는 예약 티켓입니다."),
    MEMBER_TICKET_QUANTITY(HttpStatus.BAD_REQUEST, "TICKET4001", "주문 수량이 적절하지 않습니다"),
    MEMBER_TICKET_STOCK(HttpStatus.BAD_REQUEST, "TICKET4002", "주문 수량이 재고를 초과했습니다"),
    MEMBER_TICKET_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "TICKET4003", "이미 취소하신 티켓입니다."),
    MEMBER_TICKET_WRONG_STATUS(HttpStatus.BAD_REQUEST, "TICKET4004", "티켓의 상태가 적절하지 않습니다."),

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE4000", "이미지를 찾을 수 없습니다."),
    
    //REPORT ERROR
    UNSUPPORTED_OBJECT_TYPE(HttpStatus.BAD_REQUEST, "REPORT4000", "지원하지 않는 신고 타입입니다."),

    // AMATEURSHOW ERROR
    AMATEURSHOW_NOT_FOUND(HttpStatus.NOT_FOUND, "AMATEURSHOW4000", "존재하지 않는 소극장 공연입니다."),

    // AMATEUR TICKET ERROR
    AMATEUR_TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "AMATEURTICKET4000", "존재하지 않는 소극장 공연 티켓입니다."),
    AMATEUR_TICKET_STOCK(HttpStatus.BAD_REQUEST, "AMATEURTICKET4001", "주문 수량은 최소 1개 이상이어야 합니다."),


    // PHOTOALBUM ERROR
    PHOTOALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "PHOTOALBUM4000", "존재하지 않는 사진첩입니다."),

    //BOARD ERROR
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD4001", "게시글이 존재하지 않습니다."),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BOARD4002", "수정/삭제 권한이 없습니다."),
    ONLY_PERFORMER_CAN_WRITE_PROMOTION(HttpStatus.FORBIDDEN, "BOARD4003", "홍보게시판은 Performer만 작성할 수 있습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT4001", "댓글이 존재하지 않습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COMMENT4002", "댓글 수정/삭제 권한이 없습니다."),
    COMMENT_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "COMMENT4003", "대댓글의 depth는 1까지만 허용됩니다."),
    ALREADY_REPORTED(HttpStatus.BAD_REQUEST, "REPORT4001", "이미 신고한 게시글입니다."),
    SELF_REPORT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "REPORT4002", "본인이 작성한 글/댓글은 신고할 수 없습니다."),

    //NOTICE ERROR
    MEMBERNOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBERNOTICE4001", "존재하지 않는 알림입니다."),
    // INQUIRY ERROR
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY4000", "존재하지 않는 문의글입니다.");




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
