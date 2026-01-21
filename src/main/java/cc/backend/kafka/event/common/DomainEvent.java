package cc.backend.kafka.event.common;

import cc.backend.kafka.event.common.enums.DomainEventType;

import java.io.Serializable;

public interface DomainEvent extends Serializable {
    DomainEventType getEventType();

}
