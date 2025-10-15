package cc.backend.scheduler;

import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.ticket.repository.RealTicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class ShowStatusScheduler {

    private final AmateurShowRepository amateurShowRepository;
    private final RealTicketRepository realTicketRepository;

    // ==  매일 0시 0분 5초에 실행되어 공연의 상태를 업데이트합니다. (YET -> ONGOING, ONGOING -> ENDED) == //

    @Scheduled(cron = "5 0 0 * * *") // 매일 0시 0분 5초
    @Transactional
    public void updateShowStatuses() {
        log.info("✅ 공연 상태 자동 업데이트 스케줄러 시작");
        LocalDate today = LocalDate.now();

        // 1. 'YET' -> 'ONGOING' 으로 변경
        int ongoingCount = amateurShowRepository.updateShowsToOngoing(today);
        if (ongoingCount > 0) {
            log.info("{}개의 공연이 '진행 중(ONGOING)' 상태로 변경되었습니다.", ongoingCount);
        }

        // 2. 'ONGOING' -> 'ENDED' 로 변경
        int endedCount = amateurShowRepository.updateShowsToEnded(today);
        if (endedCount > 0) {
            log.info("{}개의 공연이 '종료(ENDED)' 상태로 변경되었습니다.", endedCount);
        }

        log.info("공연 상태 자동 업데이트 스케줄러 종료");
    }

    @Scheduled(cron = "5 0 0 * * *")
    @Transactional
    public void updateTicketStatusToUsed() {
        log.info("티켓 상태(RESERVED -> USED) 자동 업데이트 스케줄러 시작");

        int updatedCount = realTicketRepository.updateReservedToUsed(LocalDateTime.now());

        if (updatedCount > 0) {
            log.info("{}개의 티켓이 '사용 완료(USED)' 상태로 변경되었습니다.", updatedCount);
        }

        log.info("티켓 상태 자동 업데이트 스케줄러 종료");
    }
}


