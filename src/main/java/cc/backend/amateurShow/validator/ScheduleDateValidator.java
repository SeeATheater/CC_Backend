package cc.backend.amateurShow.validator;


import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ScheduleDateValidator implements ConstraintValidator<ValidScheduleDate, AmateurEnrollRequestDTO> {
    @Override
    public boolean isValid(AmateurEnrollRequestDTO dto, ConstraintValidatorContext context) {

        if (dto.getStart().isAfter(dto.getEnd())) {
            // 기본 메시지 대신 ErrorStatus 메시지로 설정
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.INVALID_DATE_RANGE.getMessage())
                    .addConstraintViolation();
            return false;
        }

        return !dto.getStart().isAfter(dto.getEnd()); // start <= end
    }
}
