package cc.backend.ticket.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempTicketCreateRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void quantityMustBePositive() {
        assertQuantityViolation(0);
        assertQuantityViolation(-1);
    }

    @Test
    void positiveQuantityIsValid() {
        TempTicketCreateRequestDTO requestDTO = TempTicketCreateRequestDTO.builder()
                .quantity(1)
                .build();

        Set<ConstraintViolation<TempTicketCreateRequestDTO>> violations = validator.validate(requestDTO);

        assertTrue(violations.isEmpty());
    }

    private void assertQuantityViolation(int quantity) {
        TempTicketCreateRequestDTO requestDTO = TempTicketCreateRequestDTO.builder()
                .quantity(quantity)
                .build();

        Set<ConstraintViolation<TempTicketCreateRequestDTO>> violations = validator.validate(requestDTO);

        assertEquals(1, violations.size());
        ConstraintViolation<TempTicketCreateRequestDTO> violation = violations.iterator().next();
        assertEquals("quantity", violation.getPropertyPath().toString());
        assertEquals("TEMP_TICKET_QUANTITY", violation.getMessage());
    }
}
