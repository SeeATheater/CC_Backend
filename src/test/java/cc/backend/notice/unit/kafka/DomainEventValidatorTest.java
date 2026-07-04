package cc.backend.notice.unit.kafka;

import org.junit.jupiter.api.Test;

import static cc.backend.kafka.event.common.DomainEventValidator.requireNonNullEvent;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainEventValidatorTest {

    @Test
    void throwsCommonExceptionWhenKafkaEventIsNull() {
        assertThatThrownBy(() -> requireNonNullEvent(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kafka domain event must not be null");
    }
}
