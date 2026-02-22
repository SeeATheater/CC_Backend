package cc.backend.scheduler;

import cc.backend.ticket.entity.TempTicket;
import cc.backend.ticket.repository.TempTicketRepository;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.kakaoPay.service.KakaoPayBusinessService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TempTicketExpireScheduler {

    private final TempTicketRepository tempTicketRepository;
    private final KakaoPayBusinessService kakaoPayBusinessService;

    // 1분마다 실행
    @Scheduled(fixedDelay = 60000)
    public void expirePendingTickets() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.minusMinutes(15);

        List<TempTicket> expiredTickets =
                tempTicketRepository.findExpiredPendingTickets(expireTime);

        if (expiredTickets.isEmpty()) return;

        log.info("TTL 만료 대상 {}개", expiredTickets.size());

        for (TempTicket tempTicket : expiredTickets) {
            try {
                if (tempTicket.getReservationStatus() != ReservationStatus.PENDING)
                    continue;

                kakaoPayBusinessService.stopPayment(
                        String.valueOf(tempTicket.getId())
                );

                log.info("tempTicket {} TTL 만료 처리 완료", tempTicket.getId());

            } catch (Exception e) {
                log.error("TTL expire 실패 tempTicketId={}", tempTicket.getId(), e);
            }
        }
    }
}
