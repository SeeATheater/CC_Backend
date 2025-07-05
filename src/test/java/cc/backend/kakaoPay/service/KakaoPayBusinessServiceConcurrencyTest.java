package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KakaoPayBusinessServiceConcurrencyTest {

    @Autowired
    private KakaoPayService kakaoPayService;

    @Autowired
    private KakaoPayBusinessService kakaoPayBusinessService;

    @Autowired
    private MemberTicketRepository memberTicketRepository;

    @Autowired
    private AmateurRoundsRepository amateurRoundsRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager em;

    private Long roundId;
    private List<Long> ticketIds;

    // 동시성 테스트를 위한 데이터 초기화
    @BeforeEach
    void setUp() {

        roundId = 66L;
        ticketIds = List.of(17L, 20L, 21L);

        transactionTemplate.executeWithoutResult(status -> {
            // 재고 1개로 설정
            em.createQuery("UPDATE AmateurRounds a SET a.totalTicket = 1 WHERE a.id = :id")
                    .setParameter("id", roundId)
                    .executeUpdate();

            // 모든 티켓의 요청 수량을 1로, 상태를 PENDING으로 설정
            em.createQuery("UPDATE MemberTicket m SET m.reservationStatus = :status, m.quantity = 1 WHERE m.id IN :ids")
                    .setParameter("status", ReservationStatus.PENDING)
                    .setParameter("ids", ticketIds)
                    .executeUpdate();

            em.flush();
            em.clear();
        });
    }

    @Test
    @DisplayName("시나리오 1: 재고보다 많은 수량으로 결제 준비 요청 시, 예외가 발생해야 한다.")
    void ready_Fails_When_Stock_Is_Insufficient() {

        // given: DB 상태 직접 설정
        Long targetTicketId = ticketIds.get(0);
        transactionTemplate.executeWithoutResult(status -> {
            // 재고는 1개로 유지
            em.createQuery("UPDATE AmateurRounds a SET a.totalTicket = 1 WHERE a.id = :id")
                    .setParameter("id", roundId)
                    .executeUpdate();

            // 특정 티켓의 요청 수량만 2개로 변경
            em.createQuery("UPDATE MemberTicket m SET m.quantity = 2 WHERE m.id = :id")
                    .setParameter("id", targetTicketId)
                    .executeUpdate();
        });

        // when & then: kakaoPayService.ready()를 호출하면 예외가 발생하는지 검증
        GeneralException exception = assertThrows(GeneralException.class, () -> {
            kakaoPayService.ready(targetTicketId, "test-user-1");
        });

        assertEquals(ErrorStatus.MEMBER_TICKET_STOCK, exception.getCode());
    }

    @Test
    @DisplayName("시나리오 2: 동시에 마지막 재고 승인 요청 시, 단 한 명만 성공해야 한다.")
    void approve_Succeeds_Only_Once_Under_Concurrency() throws InterruptedException {

        // given: 재고 1개, 3명의 사용자가 각 1개씩 구매하려는 상황 (BeforeEach에서 설정됨)
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        // when: 3개의 스레드가 동시에 kakaoPayBusinessService.handleApprovedTicket() 호출
        for (Long ticketId : ticketIds) {
            String partnerOrderId = String.valueOf(ticketId);

            futures.add(executor.submit(() -> {
                try {
                    latch.await(); // 모든 스레드가 동시에 시작하도록 대기
                    kakaoPayBusinessService.handleApprovedTicket(partnerOrderId);
                    successCount.incrementAndGet();
                } catch (GeneralException e) {
                    // 재고 부족 또는 이미 처리된 티켓 예외가 발생하면 실패로 간주
                    if (e.getCode().equals(ErrorStatus.MEMBER_TICKET_STOCK) ||
                            e.getCode().equals(ErrorStatus.MEMBER_TICKET_ALREADY_RESERVED)) {
                        failCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        latch.countDown(); // 모든 스레드 동시 실행

        for (Future<?> f : futures) {
            try {
                f.get(); // 각 스레드가 끝날 때까지 대기
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

        // then: 최종 결과 검증
        assertEquals(1, successCount.get(), "정상적으로 승인된 티켓은 1개여야 합니다.");
        assertEquals(2, failCount.get(), "재고 부족 등으로 실패한 티켓은 2개여야 합니다.");

        // DB 상태 최종 확인
        AmateurRounds finalRound = amateurRoundsRepository.findById(roundId).get();
        assertEquals(0, finalRound.getTotalTicket(), "최종 재고는 0이어야 합니다.");

        long reservedCount = memberTicketRepository.findAllById(ticketIds).stream()
                .filter(t -> t.getReservationStatus().equals(ReservationStatus.RESERVED))
                .count();
        assertEquals(1, reservedCount, "예약 완료(RESERVED) 상태인 티켓은 1개여야 합니다.");
    }
}