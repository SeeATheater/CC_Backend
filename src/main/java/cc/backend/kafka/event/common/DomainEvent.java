package cc.backend.kafka.event.common;

import cc.backend.kafka.event.common.enums.DomainEventType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

public interface DomainEvent extends Serializable {
    String eventId();

    @JsonIgnore
    DomainEventType getEventType();

}
