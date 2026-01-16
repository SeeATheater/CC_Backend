package cc.backend.notice.kafka;

import cc.backend.notice.kafka.NewShowEvent.MemberRecommendationEvent;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaRecommendConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // Producer: Object 직렬화(모든 이벤트 공용)
    @Bean
    public ProducerFactory<String, MemberRecommendationEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean public KafkaTemplate<String, MemberRecommendationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // Consumer: Object 역직렬화(모든 이벤트 공용)
    @Bean
    public ConsumerFactory<String, MemberRecommendationEvent> consumerFactory() {
        JsonDeserializer<MemberRecommendationEvent> deserializer = new JsonDeserializer<>(MemberRecommendationEvent.class);
        deserializer.addTrustedPackages("*"); // 패키지 제한 없앰

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    // 재시도 + DLQ 설정
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, MemberRecommendationEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate,
                        (record, ex) -> new TopicPartition(record.topic() + "-dlq", record.partition()));

        // 1초 간격, 최대 3회 재시도
        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MemberRecommendationEvent> kafkaListenerContainerFactory(KafkaTemplate<String, MemberRecommendationEvent> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, MemberRecommendationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 컨슈머 병렬 처리 스레드 수
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate)); //에러발생시 처리 로직
        return factory;
    }


}
