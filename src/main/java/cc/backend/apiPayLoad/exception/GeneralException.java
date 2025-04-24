package cc.backend.apiPayLoad.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import cc.backend.apiPayLoad.code.BaseErrorCode;
import cc.backend.apiPayLoad.code.ReasonDTO;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException{
    private BaseErrorCode code;

    public ReasonDTO getErrorReason() {
        return this.code.getErrorReason();
    }

    public ReasonDTO getErrorReasonHttpStatus(){
        return this.code.getErrorReasonHttpStatus();
    }
}
