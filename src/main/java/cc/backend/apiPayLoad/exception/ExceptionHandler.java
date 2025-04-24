package cc.backend.apiPayLoad.exception;

import cc.backend.apiPayLoad.code.BaseErrorCode;

public class ExceptionHandler extends GeneralException {
    public ExceptionHandler(BaseErrorCode code) {
        super(code);
    }
}