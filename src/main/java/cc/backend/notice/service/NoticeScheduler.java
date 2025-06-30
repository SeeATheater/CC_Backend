package cc.backend.notice.service;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.member.entity.Member;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeScheduler {
    private final MemberTicketRepository memberTicketRepository;
    private final NoticeRepository noticeRepository;
    private final MemberNoticeRepository memberNoticeRepository;

    /**
     * 매일 자정에 오늘 공연 예정인 티켓 보유자들에게 알림 전송
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00 실행
    public void notifyTodayPerformances() {
        LocalDate today = LocalDate.now();

        // 오늘 공연 티켓 전체 조회
        List<MemberTicket> tickets = memberTicketRepository.findAllByPerformanceDate(today);

        // 공연별로 티켓 그룹핑
        Map<Long, List<MemberTicket>> ticketsByShowId = tickets.stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getAmateurTicket().getAmateurShow().getId()));

        // 공연별로 Notice 및 MemberNotice 생성
        for (Map.Entry<Long, List<MemberTicket>> entry : ticketsByShowId.entrySet()) {
            Long amateurShowId = entry.getKey();
            List<MemberTicket> showTickets = entry.getValue();

            AmateurShow show = showTickets.get(0).getAmateurTicket().getAmateurShow();

            // 공연에 대한 Notice 생성
            Notice notice = noticeRepository.save(
                    Notice.builder()
                            .type(NoticeType.REMIND)
                            .contentId(amateurShowId)
                            .message("오늘은 \"" + show.getName() + "\"의 공연날입니다.")
                            .build()
            );

            // 이 공연을 예매한 모든 멤버에 대해 MemberNotice 생성
            List<MemberNotice> memberNotices = showTickets.stream()
                    .map(MemberTicket::getMember) // 멤버 추출
                    .distinct() // 멤버 중복 제거
                    .map(member -> MemberNotice.builder()
                            .notice(notice)
                            .member(member)
                            .build())
                    .toList();

            memberNoticeRepository.saveAll(memberNotices);

        }
    }
}
