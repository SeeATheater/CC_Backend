package cc.backend.kafka.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kafka.entity.OutboxEvent;
import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY) //비즈니스 데이터와 outbox 이벤트 저장을 같은 트랜잭션으로 강제
    public void appendOutboxEvent(DomainEvent event, String topic, String key) {
        try{
            outboxEventRepository.save(
                    OutboxEvent.pending(
                            event,
                            topic,
                            key,
                            objectMapper.writeValueAsString(event)
                    )
            );
        } catch(JsonProcessingException e){
            throw new GeneralException(ErrorStatus.SERIALIZATION_FAIL);
        }
    }

}
