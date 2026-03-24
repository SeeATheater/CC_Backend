package cc.backend.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicConfig {

    @Bean
    public NewTopic approvalShowTopic() {
        return TopicBuilder.name("approval-show-topic")
                .partitions(1)       // 파티션 수
                .replicas(1)         // 단일 브로커(Docker 1대) 환경
                .build();
    }
}
