package cc.backend.kafka;

import cc.backend.kafka.event.common.DomainEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        /*
        일반 발행 메시지의 value 타입: DomainEvent
        DeadLetterPublishingRecoverer가 역직렬화에 실패한 메시지를 DLQ로 보낼 때 value 타입: 원본 byte[] 타입

        DelegatingByTypeSerializer를 활용하여 실제 value 타입에 맞는 Serializer를 선택
        - DomainEvent -> JsonSerializer: 정상 이벤트를 JSON으로 발행
        - byte[]      -> ByteArraySerializer: 역직렬화 실패 원본을 그대로 DLQ로 발행

        -> 하나의 KafkaTemplate이 DomainEvent와 byte[]를 모두 직렬화할 수 있게 정상 이벤트와 실패 payload의 Serializer 분리
        */
        Map<Class<?>, Serializer<?>> serializers = new LinkedHashMap<>();
        serializers.put(byte[].class, new ByteArraySerializer());
        serializers.put(DomainEvent.class, new JsonSerializer<>());

        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new DelegatingByTypeSerializer(serializers, true)
        );
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, DomainEvent> consumerFactory() {
        JsonDeserializer<DomainEvent> deserializer = new JsonDeserializer<>(DomainEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false);

        /*
        malformed JSON 처리 흐름:
        1. JsonDeserializer가 poll() 단계에서 역직렬화 실패
        2. ErrorHandlingDeserializer가 예외와 원본 payload 정보를 보존
        3. Listener 호출 전 DefaultErrorHandler로 예외 전달
        4. DeserializationException을 재시도 불가 예외로 분류
        5. DeadLetterPublishingRecoverer가 원본 byte[]를 {원본 topic}-dlq에 발행
        6. 실패 메시지를 격리한 뒤 Consumer가 다음 offset 처리

        JsonDeserializer를 단독으로 사용하면 2번 처리가 없어 같은 offset에서
        Consumer가 malformed JSON에 대해 역직렬화 시도를 반복할 수 있으므로
        역직렬화 예외를 일반 Listener 처리 예외와 구분해 ErrorHandler에 전달할 수 있는
        ErrorHandlingDeserializer로 감싼다.
        */
        ErrorHandlingDeserializer<DomainEvent> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(deserializer);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + "-dlq", record.partition())
        );
        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));

        // malformed JSON은 같은 payload인 이상 재시도 의미가 없으므로 DeserializationException은 Recoverer가 즉시 DLQ로 격리
        errorHandler.addNotRetryableExceptions(DeserializationException.class);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> highThroughputFactory(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
        return factory;
    }
}
