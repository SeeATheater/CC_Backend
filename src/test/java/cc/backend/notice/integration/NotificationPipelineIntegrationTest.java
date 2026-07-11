package cc.backend.notice.integration;

import cc.backend.kafka.KafkaConfig;
import cc.backend.kafka.TopicConfig;
import cc.backend.kafka.entity.OutboxEvent;
import cc.backend.kafka.entity.OutboxStatus;
import cc.backend.kafka.entity.ProcessedEvent;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.approvalShowEvent.consumer.ApprovalConsumer;
import cc.backend.kafka.event.approvalShowEvent.consumer.LikerConsumer;
import cc.backend.kafka.event.approvalShowEvent.consumer.RecommendConsumer;
import cc.backend.kafka.event.commentEvent.CommentConsumer;
import cc.backend.kafka.event.commentEvent.CommentEvent;
import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.publisher.KafkaOutboxRelay;
import cc.backend.kafka.repository.OutboxEventRepository;
import cc.backend.kafka.repository.ProcessedEventRepository;
import cc.backend.notice.service.NoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 임시 MySQL과 Kafka를 사용해 Outbox 발행 및 Consumer 멱등성을 검증하는 통합 테스트.
 * 테스트 종료 시 컨테이너와 데이터가 삭제되므로 개발 DB에는 영향을 주지 않는다.
 * NoticeService는 Mock으로 두어 실제 알림 생성보다 메시지 파이프라인에 집중한다.
 *
 * <pre>
 * 시나리오 1: ProcessedEvent 선점
 *     -> 같은 (eventId, consumerGroup)을 두 번 INSERT IGNORE
 *     -> 최초 반환값 1, 중복 반환값 0
 *
 * 시나리오 2: 승인 이벤트 전달
 *     -> MySQL에 PENDING Outbox 저장
 *     -> KafkaOutboxRelay가 polling 후 Kafka 발행
 *     -> Approval/Liker/Recommend Consumer가 각자 메시지 소비
 *     -> Consumer Group별 ProcessedEvent 총 3건 생성
 *     -> 동일 이벤트를 다시 발행해도 NoticeService는 추가 호출되지 않음
 *
 * 시나리오 3: malformed JSON 격리와 다음 offset 처리
 *     -> raw byte[] malformed JSON을 comment-created-topic에 발행
 *     -> 재시도 없이 comment-created-topic-dlq로 원본 byte[] 이동
 *     -> 다음 정상 CommentEvent에서 Listener 일시 실패 2회 후 3번째 처리 성공
 *     -> ProcessedEvent 저장 및 Consumer 진행 확인
 * </pre>
 */
@Testcontainers
@DirtiesContext
@SpringBootTest(
        classes = NotificationPipelineIntegrationTest.TestApplication.class,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.properties.hibernate.show_sql=false",
                "spring.kafka.consumer.group-id=notification-integration-test",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "outbox.polling-delay-ms=100",
                "outbox.max-attempts=3"
        }
)
final class NotificationPipelineIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.4");

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeService noticeService;

    @Test
    @Transactional
    void processedEventClaimReturnsOneThenZeroForSameConsumerGroup() {
        String eventId = UUID.randomUUID().toString();
        String consumerGroup = "processed-event-claim-test-group";

        int firstClaim = processedEventRepository.insertIfAbsent(
                eventId,
                consumerGroup
        );
        int duplicateClaim = processedEventRepository.insertIfAbsent(
                eventId,
                consumerGroup
        );

        assertThat(firstClaim).isEqualTo(1);
        assertThat(duplicateClaim).isZero();
    }

    @Test
    void approvalOutboxIsPublishedAndHandledOnceByEachConsumerGroup() throws Exception {
        ApprovalShowEvent event = ApprovalShowEvent.create(101L, 202L);
        OutboxEvent outboxEvent = OutboxEvent.pending(
                event,
                "approval-show-topic",
                event.amateurShowId().toString(),
                objectMapper.writeValueAsString(event)
        );

        Long outboxId = outboxEventRepository.saveAndFlush(outboxEvent).getId();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            OutboxEvent published = outboxEventRepository.findById(outboxId).orElseThrow();
            assertThat(published.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            assertThat(processedEventRepository.count()).isEqualTo(3);

            verify(noticeService).notifyApproval(any(ApprovalShowEvent.class));
            verify(noticeService).notifyLikers(any(ApprovalShowEvent.class));
            verify(noticeService).notifyRecommendation(any(ApprovalShowEvent.class));
        });

        kafkaTemplate.send(
                "approval-show-topic",
                event.amateurShowId().toString(),
                event
        ).get();

        verify(noticeService, after(3000).times(1))
                .notifyApproval(any(ApprovalShowEvent.class));
        verify(noticeService, after(3000).times(1))
                .notifyLikers(any(ApprovalShowEvent.class));
        verify(noticeService, after(3000).times(1))
                .notifyRecommendation(any(ApprovalShowEvent.class));
        assertThat(processedEventRepository.count()).isEqualTo(3);
    }

    @Test
    void malformedJsonIsPublishedToDlqAsOriginalBytesAndNextRecordIsRetried() throws Exception {
        String sourceTopic = "comment-created-topic";
        String dlqTopic = sourceTopic + "-dlq";
        byte[] malformedPayload = "{not-valid-json".getBytes(StandardCharsets.UTF_8);

        try (KafkaConsumer<String, byte[]> dlqConsumer = createDlqConsumer()) {
            dlqConsumer.subscribe(List.of(dlqTopic));

            publishMalformedJson(sourceTopic, malformedPayload);

            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                ConsumerRecords<String, byte[]> records =
                        dlqConsumer.poll(Duration.ofMillis(500));

                assertThat(records)
                        .extracting(ConsumerRecord::value)
                        .anySatisfy(value -> assertThat(value)
                                .containsExactly(malformedPayload));
            });
        }

        CommentEvent validEvent = CommentEvent.create(301L, 302L, 303L, 304L);
        when(noticeService.notifyNewComment(any(CommentEvent.class)))
                .thenThrow(new IllegalStateException("temporary notice failure"))
                .thenThrow(new IllegalStateException("temporary notice failure"))
                .thenReturn(null);

        kafkaTemplate.send(sourceTopic, validEvent.boardId().toString(), validEvent).get();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            verify(noticeService, org.mockito.Mockito.times(3))
                    .notifyNewComment(any(CommentEvent.class));
            assertThat(processedEventRepository.existsByEventIdAndConsumerGroup(
                    validEvent.eventId(),
                    "comment-notice-group"
            )).isTrue();
        });
    }

    private void publishMalformedJson(String topic, byte[] payload) throws Exception {
        Map<String, Object> properties = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
        );

        try (KafkaProducer<String, byte[]> producer = new KafkaProducer<>(properties)) {
            producer.send(new ProducerRecord<>(topic, "malformed", payload)).get();
        }
    }

    private KafkaConsumer<String, byte[]> createDlqConsumer() {
        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlq-verification-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class
        );
        return new KafkaConsumer<>(properties);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableKafka
    @EnableScheduling
    @EntityScan(basePackageClasses = {OutboxEvent.class, ProcessedEvent.class})
    @EnableJpaRepositories(basePackageClasses = {
            OutboxEventRepository.class,
            ProcessedEventRepository.class
    })
    @Import({
            KafkaConfig.class,
            TopicConfig.class,
            KafkaOutboxRelay.class,
            ApprovalConsumer.class,
            LikerConsumer.class,
            RecommendConsumer.class,
            CommentConsumer.class,
            NoticeServiceTestConfig.class
    })
    static class TestApplication {
    }

    @TestConfiguration
    static class NoticeServiceTestConfig {

        @Bean
        NoticeService noticeService() {
            return mock(NoticeService.class);
        }
    }
}
