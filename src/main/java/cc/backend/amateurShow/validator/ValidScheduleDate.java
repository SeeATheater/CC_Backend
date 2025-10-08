package cc.backend.amateurShow.validator;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ScheduleDateValidator.class)
public @interface ValidScheduleDate {
    String message() default "종료 날짜는 시작 날짜 이후여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
