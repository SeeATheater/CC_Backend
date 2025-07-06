package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import jakarta.persistence.EntityManager;
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
    private AmateurRoundsRepository amateurRoundsRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("시나리오 1: 재고보다 많은 수량으로 결제 준비 요청 시, 예외가 발생해야 한다.")
    void ready_Fails_When_Stock_Is_Insufficient() {

        // given: 회차에 남은 재고는 1개, 사용자는 2개 구매 요청
        Long roundId = 66L;
        Long targetTicketId = 17L;

        transactionTemplate.executeWithoutResult(status -> {
            em.createQuery("UPDATE AmateurRounds a SET a.totalTicket = 1 WHERE a.id = :id")
                    .setParameter("id", roundId).executeUpdate();
            em.createQuery("UPDATE MemberTicket m SET m.quantity = 2 WHERE m.id = :id")
                    .setParameter("id", targetTicketId).executeUpdate();
        });

        // when & then: 결제 준비 요청 시 재고 부족 예외가 발생하는지 검증
        GeneralException exception = assertThrows(GeneralException.class,
                () -> kakaoPayService.ready(targetTicketId, "test-user-1"));

        assertEquals(ErrorStatus.MEMBER_TICKET_STOCK, exception.getCode());
    }

    @Test
    @DisplayName("시나리오 2: 동시에 마지막 재고에 대해 결제 준비 요청 시, 단 한 명만 성공해야 한다.")
    void concurrentReady_With_One_Stock_Only_One_Succeeds() {

        // given: 재고 1개, 3명의 사용자가 각각 1개씩 동시에 구매하려는 상황
        Long roundId = 66L;
        List<Long> ticketIds = List.of(17L, 28L, 29L);

        transactionTemplate.executeWithoutResult(status -> {
            em.createQuery("UPDATE AmateurRounds a SET a.totalTicket = 1 WHERE a.id = :id")
                    .setParameter("id", roundId).executeUpdate();
            em.createQuery("UPDATE MemberTicket m SET m.quantity = 1 WHERE m.id IN :ids")
                    .setParameter("ids", ticketIds).executeUpdate();
        });

        int threadCount = 3; // 스레드 3개
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0); // 성공한 사용자 수
        AtomicInteger failCount = new AtomicInteger(0); // 실패한 사용자 수
        List<Future<?>> futures = new ArrayList<>();

        // when: 3개의 스레드가 동시에 결제 준비 요청
        for (Long ticketId : ticketIds) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await(); // 모든 스레드가 동시에 시작하도록 대기
                    kakaoPayService.ready(ticketId, "concurrent-user");
                    successCount.incrementAndGet(); // 성공한 사용자 카운트
                } catch (GeneralException e) {
                    if (e.getCode().equals(ErrorStatus.MEMBER_TICKET_STOCK)) {
                        failCount.incrementAndGet(); // 실패한 사용자 카운트
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        latch.countDown(); // 모든 스레드 동시 실행

        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { e.printStackTrace(); }
        }
        executor.shutdown();

        // then: 성공 1명, 실패 2명이어야 함
        assertEquals(1, successCount.get(), "정상적으로 준비된 요청은 1개여야 합니다.");
        assertEquals(2, failCount.get(), "재고 부족으로 실패한 요청은 2개여야 합니다.");

        // 최종 재고 개수는 0개
        AmateurRounds finalRound = amateurRoundsRepository.findById(roundId)
                .orElseThrow(() -> new IllegalStateException("해당 회차가 존재하지 않습니다."));

        assertEquals(0, finalRound.getTotalTicket(), "최종 재고는 0이어야 합니다.");
    }

    @Test
    @DisplayName("시나리오 3: 재고가 충분해 보여도 동시 요청 시, 재고가 허락하는 만큼만 성공해야 한다 (TOCTOU 방지 검증).")
    void concurrentReady_With_Sufficient_Stock_But_Limited_Success() {

        // given: 재고 4개, 3명의 사용자가 각각 2개씩 구매하려는 상황 (총 6개 요청)
        Long roundId = 66L;
        List<Long> ticketIds = List.of(17L, 28L, 29L);

        transactionTemplate.executeWithoutResult(status -> {
            em.createQuery("UPDATE AmateurRounds a SET a.totalTicket = 4 WHERE a.id = :id")
                    .setParameter("id", roundId).executeUpdate();
            em.createQuery("UPDATE MemberTicket m SET m.quantity = 2 WHERE m.id IN :ids")
                    .setParameter("ids", ticketIds).executeUpdate();
        });

        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        // when: 3개의 스레드가 동시에 결제 준비 요청
        for (Long ticketId : ticketIds) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    kakaoPayService.ready(ticketId, "concurrent-user-2");
                    successCount.incrementAndGet();
                } catch (GeneralException e) {
                    if (e.getCode().equals(ErrorStatus.MEMBER_TICKET_STOCK)) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        latch.countDown();

        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception e) { e.printStackTrace(); }
        }
        executor.shutdown();

        // then: 2명(4개)은 성공하고, 마지막 1명은 재고가 없어 실패해야 함.
        assertEquals(2, successCount.get(), "성공은 2건이어야 합니다.");
        assertEquals(1, failCount.get(), "실패는 1건이어야 합니다.");

        // 최종 재고 개수는 0개
        AmateurRounds finalRound = amateurRoundsRepository.findById(roundId)
                .orElseThrow(() -> new IllegalStateException("해당 회차가 존재하지 않습니다."));

        assertEquals(0, finalRound.getTotalTicket(), "최종 재고는 0이어야 합니다.");
    }
}