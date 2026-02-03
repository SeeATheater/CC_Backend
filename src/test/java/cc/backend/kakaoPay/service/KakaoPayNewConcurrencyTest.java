package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.AmateurTicketRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.ticket.entity.TempTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.TempTicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("prod") // 또는 dev
class KakaoPayNewConcurrencyTest {

    @Autowired
    private KakaoPayBusinessService kakaoPayBusinessService;

    @Autowired
    private AmateurRoundsRepository amateurRoundsRepository;

    @Autowired
    private AmateurShowRepository amateurShowRepository;

    @Autowired
    private AmateurTicketRepository amateurTicketRepository;

    @Autowired
    private TempTicketRepository tempTicketRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("시나리오 1: 재고보다 많은 수량으로 결제 준비 요청 시, 예외가 발생해야 한다.")
    void scenario1_ready_Fails_When_Stock_Is_Insufficient() {
        // given: 재고 1개, 사용자는 2개 구매 요청
        Long[] ids = transactionTemplate.execute(status -> {
            Member member = Member.builder().name("테스터1").build();
            memberRepository.save(member);
            AmateurShow show = AmateurShow.builder().name("공연1").detailAddress("주소").build();
            amateurShowRepository.save(show);
            AmateurTicket ticket = AmateurTicket.builder().amateurShow(show).discountName("할인").price(1000).build();
            amateurTicketRepository.save(ticket);
            AmateurRounds round = AmateurRounds.builder().amateurShow(show).totalTicket(1).performanceDateTime(LocalDateTime.now().plusDays(1)).build();
            amateurRoundsRepository.save(round);

            TempTicket tempTicket = TempTicket.builder()
                                              .amateurRound(round).amateurTicket(ticket).member(member)
                                              .quantity(2).totalPrice(2000).reservationStatus(ReservationStatus.PENDING).build();
            tempTicketRepository.save(tempTicket);
            return new Long[]{tempTicket.getId(), member.getId()};
        });

        // when & then
        assertThrows(GeneralException.class, () ->
            kakaoPayBusinessService.preparePayment(ids[0], String.valueOf(ids[1]))
        );
    }

    @Test
    @DisplayName("시나리오 2: 동시에 마지막 재고에 대해 결제 준비 요청 시, 단 한 명만 성공해야 한다.")
    void concurrentReady_With_One_Stock_Only_One_Succeeds() throws InterruptedException {
        // 1. Given: 필수 연관 데이터 생성
        Long[] setupIds = transactionTemplate.execute(status -> {
            Member member = Member.builder()
                                  .name("공통회원")
                                  .build();
            memberRepository.save(member);

            AmateurShow show = AmateurShow.builder()
                                          .name("테스트공연")
                                          .detailAddress("서울시 강남구")
                                          .build();
            amateurShowRepository.save(show);

            AmateurTicket ticket = AmateurTicket.builder()
                                                .amateurShow(show)
                                                .discountName("일반권")
                                                .price(10000) // 티켓 가격 설정
                                                .build();
            amateurTicketRepository.save(ticket);

            AmateurRounds round = AmateurRounds.builder()
                                               .amateurShow(show)
                                               .performanceDateTime(LocalDateTime.now().plusDays(7))
                                               .totalTicket(1) // 재고 1개
                                               .build();
            amateurRoundsRepository.save(round);

            return new Long[]{round.getId(), member.getId(), ticket.getId()};
        });

        Long roundId = setupIds[0];
        Long memberId = setupIds[1];
        Long ticketId = setupIds[2];

        // 2. 임시 티켓 3개 생성 (totalPrice를 0보다 크게 설정)
        List<Long> tempTicketIds = transactionTemplate.execute(status -> {
            AmateurRounds round = amateurRoundsRepository.findById(roundId).get();
            Member member = memberRepository.findById(memberId).get();
            AmateurTicket ticket = amateurTicketRepository.findById(ticketId).get();

            List<Long> ids = new ArrayList<>();
            for(int i=0; i<3; i++) {
                TempTicket t = TempTicket.builder()
                                         .amateurRound(round)
                                         .amateurTicket(ticket)
                                         .member(member)
                                         .quantity(1)
                                         .totalPrice(10000)
                                         .reservationStatus(ReservationStatus.PENDING)
                                         .build();
                tempTicketRepository.save(t);
                ids.add(t.getId());
            }
            return ids;
        });

        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 3. When: 실제 서비스 호출
        for (Long ttId : tempTicketIds) {
            executor.submit(() -> {
                try {
                    latch.await();
                    // preparePayment 호출
                    kakaoPayBusinessService.preparePayment(ttId, String.valueOf(memberId));
                    successCount.incrementAndGet();
                } catch (GeneralException e) {
                    // 재고 선점 실패 시 발생하는 예외 캐치
                    if (e.getCode().equals(ErrorStatus.TEMP_TICKET_STOCK)) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // 카카오 API 응답 에러가 나더라도 재고는 이미 깎인 상태임
                    System.out.println("API 혹은 기타 에러 발생: " + e.getMessage());
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        // 4. Then: 동시성 제어 결과 확인
        // 성공하거나(API까지 성공), 혹은 API에서 에러가 났더라도 재고 선점을 통과한 건수는 1건이어야 함
        // failCount가 2라는 것은 나머지 2명은 확실히 재고 부족으로 걸러졌다는 뜻
        assertEquals(2, failCount.get(), "재고 부족으로 실패한 요청은 2개여야 합니다.");

        AmateurRounds finalRound = amateurRoundsRepository.findById(roundId).orElseThrow();
        assertEquals(0, finalRound.getTotalTicket(), "최종 재고는 0이어야 합니다.");
    }

    @Test
    @DisplayName("시나리오 3: 재고 4개일 때 3명이 2개씩 요청 시 2명만 성공해야 한다.")
    void concurrentReady_With_Sufficient_Stock_But_Limited_Success() throws InterruptedException {
        Long[] setupIds = transactionTemplate.execute(status -> {
            Member member = Member.builder().name("회원2").build();
            memberRepository.save(member);
            AmateurShow show = AmateurShow.builder().name("공연2").detailAddress("공연장").build();
            amateurShowRepository.save(show);
            AmateurTicket ticket = AmateurTicket.builder().amateurShow(show).discountName("할인").price(5000).build();
            amateurTicketRepository.save(ticket);
            AmateurRounds round = AmateurRounds.builder().amateurShow(show).totalTicket(4).build();
            amateurRoundsRepository.save(round);
            return new Long[]{round.getId(), member.getId(), ticket.getId()};
        });

        List<Long> tempTicketIds = transactionTemplate.execute(status -> {
            AmateurRounds round = amateurRoundsRepository.findById(setupIds[0]).get();
            Member member = memberRepository.findById(setupIds[1]).get();
            AmateurTicket ticket = amateurTicketRepository.findById(setupIds[2]).get();
            List<Long> ids = new ArrayList<>();
            for(int i=0; i<3; i++) {
                TempTicket t = TempTicket.builder()
                                         .amateurRound(round).amateurTicket(ticket).member(member)
                                         .quantity(2).totalPrice(10000).reservationStatus(ReservationStatus.PENDING).build();
                tempTicketRepository.save(t);
                ids.add(t.getId());
            }
            return ids;
        });

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger failCount = new AtomicInteger(0);

        for (Long ttId : tempTicketIds) {
            executor.submit(() -> {
                try {
                    latch.await();
                    kakaoPayBusinessService.preparePayment(ttId, String.valueOf(setupIds[1]));
                } catch (GeneralException e) {
                    if (e.getCode().equals(ErrorStatus.TEMP_TICKET_STOCK)) {
                        failCount.incrementAndGet();
                    }
                } catch (Exception ignored) {}
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        assertEquals(1, failCount.get(), "재고 부족 실패는 정확히 1건이어야 합니다.");
        AmateurRounds finalRound = amateurRoundsRepository.findById(setupIds[0]).orElseThrow();
        assertEquals(0, finalRound.getTotalTicket(), "재고는 모두 소진되어 0이어야 합니다.");
    }
}