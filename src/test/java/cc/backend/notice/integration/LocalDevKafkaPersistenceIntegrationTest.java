package cc.backend.notice.integration;

import cc.backend.kafka.KafkaConfig;
import cc.backend.kafka.entity.OutboxEvent;
import cc.backend.kafka.entity.OutboxStatus;
import cc.backend.kafka.event.hotBoardEvent.HotBoardConsumer;
import cc.backend.kafka.event.hotBoardEvent.HotBoardEvent;
import cc.backend.kafka.publisher.KafkaOutboxRelay;
import cc.backend.kafka.repository.OutboxEventRepository;
import cc.backend.kafka.repository.ProcessedEventRepository;
import cc.backend.notice.service.NoticeServiceImpl;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 실제 개발 DB에 결과를 남기는 수동 Kafka 통합 테스트.
 *
 * <p>실제 시나리오:</p>
 * <pre>
 * 개발 DB에 PENDING OutboxEvent 저장
 *     -> KafkaOutboxRelay가 DB를 polling
 *     -> Testcontainers Kafka의 hot-board-topic으로 발행
 *     -> HotBoardConsumer가 메시지 수신
 *     -> (eventId, hot-board-notice-group)을 ProcessedEvent에 선점
 *     -> 회원 4의 Notice + MemberNotice 저장
 *     -> OutboxEvent를 PUBLISHED로 변경
 * </pre>
 *
 * <p>MySQL은 컨테이너가 아니다. {@code dev} 프로필의 DEV_DB_* 연결을 그대로
 * 사용하므로 테스트가 끝나도 outbox_event, processed_event, notice,
 * member_notice 데이터가 롤백되거나 삭제되지 않는다.</p>
 * <p>Kafka만 Testcontainers로 실행하며, NoticeService를 포함한 알림 처리 구성요소는
 * 모두 실제 Spring Bean을 사용한다.</p>
 *
 * <p>의도하지 않은 개발 DB 변경을 막기 위해 아래 환경변수가 있을 때만 실행된다.</p>
 * <pre>
 * RUN_LOCAL_KAFKA_PERSISTENCE_TEST=true \
 * ./gradlew test --tests \
 * 'cc.backend.notice.integration.LocalDevKafkaPersistenceIntegrationTest'
 * </pre>
 */
@Testcontainers
@ActiveProfiles({"dev", "local-kafka-persistence-test"})
@SpringBootTest(classes = LocalDevKafkaPersistenceIntegrationTest.TestApplication.class)
@EnabledIfEnvironmentVariable(
        named = "RUN_LOCAL_KAFKA_PERSISTENCE_TEST",
        matches = "true"
)
class LocalDevKafkaPersistenceIntegrationTest {

    private static final long TARGET_MEMBER_ID = 4L;
    private static final String CONSUMER_GROUP = "hot-board-notice-group";

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );

    @DynamicPropertySource
    static void registerKafka(DynamicPropertyRegistry registry) {
        // DB 설정은 덮어쓰지 않는다. Kafka 주소만 임시 컨테이너로 교체한다.
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    }

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberNoticeRepository memberNoticeRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leavesCompleteKafkaNotificationPipelineResultInDevDatabase() throws Exception {
        assertThat(memberRepository.existsById(TARGET_MEMBER_ID))
                .as("개발 DB에 알림 수신 회원 ID 4가 있어야 한다")
                .isTrue();

        // 실제 게시글을 참조하지 않는 HOT 알림이므로 충돌 가능성이 낮은 음수 ID를 쓴다.
        long scenarioContentId = -Math.abs(UUID.randomUUID().getMostSignificantBits());
        HotBoardEvent event = HotBoardEvent.create(scenarioContentId, TARGET_MEMBER_ID);

        OutboxEvent outbox = OutboxEvent.pending(
                event,
                "hot-board-topic",
                scenarioContentId + "-local-persistence-test",
                objectMapper.writeValueAsString(event)
        );
        Long outboxId = outboxEventRepository.saveAndFlush(outbox).getId();

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            assertThat(outboxEventRepository.findById(outboxId).orElseThrow().getStatus())
                    .isEqualTo(OutboxStatus.PUBLISHED);

            assertThat(processedEventRepository.existsByEventIdAndConsumerGroup(
                    event.eventId(),
                    CONSUMER_GROUP
            )).isTrue();

            assertThat(noticeRepository.existsByContentIdAndType(
                    scenarioContentId,
                    NoticeType.HOT
            )).isTrue();
            assertThat(memberNoticeRepository.existsByMemberIdAndNoticeContentId(
                    TARGET_MEMBER_ID,
                    scenarioContentId
            )).isTrue();
        });
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableKafka
    @EnableScheduling
    @EnableJpaAuditing
    @EntityScan(basePackages = "cc.backend")
    @EnableJpaRepositories(basePackages = {
            "cc.backend.kafka.repository",
            "cc.backend.member.repository",
            "cc.backend.notice.repository",
            "cc.backend.board.repository",
            "cc.backend.amateurShow.repository",
            "cc.backend.memberLike.repository"
    })
    @Import({
            KafkaConfig.class,
            KafkaOutboxRelay.class,
            HotBoardConsumer.class,
            NoticeServiceImpl.class
    })
    static class TestApplication {
    }
}
