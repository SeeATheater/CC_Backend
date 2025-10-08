package cc.backend.amateurShow.validator;


import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ScheduleDateValidator implements ConstraintValidator<ValidScheduleDate, AmateurEnrollRequestDTO> {
    @Override
    public boolean isValid(AmateurEnrollRequestDTO dto, ConstraintValidatorContext context) {

        return !dto.getStart().isAfter(dto.getEnd()); // start <= end
    }
}
