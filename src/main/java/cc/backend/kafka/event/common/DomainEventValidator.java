package cc.backend.kafka.event.common;

public final class DomainEventValidator {

    private DomainEventValidator() {
    }

    public static void requireNonNullEvent(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Kafka domain event must not be null");
        }
    }
}
