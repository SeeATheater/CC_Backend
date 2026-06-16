package cc.backend.config.jwt.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void refreshTokenMustNotBeBlank() {
        RefreshTokenRequest request = new RefreshTokenRequest(" ");

        assertThat(validator.validate(request))
                .singleElement()
                .satisfies(violation -> assertThat(violation.getMessage()).isEqualTo("_BAD_REQUEST"));
    }
}
