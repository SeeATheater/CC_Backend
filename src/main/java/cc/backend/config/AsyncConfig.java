package cc.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

//aws 플랜(Micro/Small) 반영해서 디폴트 설정보다 보수적으로 커스텀
@Configuration
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(0);    // 최소 스레드 없음, 필요할 때만 생성
        executor.setMaxPoolSize(5);    // 동시에 실행할 최대 스레드 수
        executor.setQueueCapacity(50); // 큐에 대기할 수 있는 작업 수

        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}