package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.ticket.repository.MemberTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class KakaoPayBusinessServiceConcurrencyTest {

    @Autowired
    KakaoPayBusinessService kakaoPayBusinessService;

    @Autowired
    MemberTicketRepository memberTicketRepository;

    @Autowired
    AmateurRoundsRepository amateurRoundsRepository;

    Long roundId;
    List<Long> ticketIds;

    @BeforeEach
    void setUp() {
        roundId = 66L; // 테스트 할 회차 id (db에 넣기)
        ticketIds = List.of(17L, 18L, 19L); // 테스트 할 PENDING 상태의 같은 티켓 3개 id
    }

    @Test
    void testConcurrentApprovalOnlyOneSuccess() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(3); // 동시에 3개 작업 실행
        CountDownLatch latch = new CountDownLatch(1); // 모든 스레드가 동시에 시작하도록

        // 성공/실패
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();

        for (Long ticketId : ticketIds) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await(); // 모든 스레드가 동시에 시작
                    kakaoPayBusinessService.handleApprovedTicket(ticketId.toString()); // 결제 승인 로직 실행
                    success.incrementAndGet();
                } catch (GeneralException e) {
                    fail.incrementAndGet();
                } catch (InterruptedException ignored) {}
            }));
        }

        latch.countDown(); // 모든 스레드를 동시에 실행

        for (Future<?> f : futures) {
            f.get(); // 각 스레드가 다 끝날 때까지 기다림
        }

        // 성공은 1건, 실패는 2건이어야 함
        assertEquals(1, success.get(), "정상적으로 결제 승인된 티켓은 1개여야 합니다");
        assertEquals(2, fail.get(), "재고 부족으로 실패한 티켓은 2개여야 합니다");

        // 실제 DB에서 재고가 0인지 확인
        AmateurRounds round = amateurRoundsRepository.findById(roundId).orElseThrow();
        assertEquals(0, round.getTotalTicket(), "잔여 재고는 0이어야 합니다");
    }
}
