package cc.backend.performer;


import cc.backend.amateurShow.converter.AmateurConverter;
import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.performer.dto.PerformerMyShowResponseDTO;
import cc.backend.performer.dto.ShowReservationResponseDTO;
import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.RealTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformerService {
    private final AmateurShowRepository amateurShowRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;
    private final RealTicketRepository realTicketRepository;

    public ShowReservationResponseDTO getShowReservationList(Long memberId, Long amateurShowId, Long roundId) {

        // 1) 공연 로드
        AmateurShow show = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        // 1-1) 로그인한 계정이 공연의 주인인지 확인
        if (!show.getMember().getId().equals(memberId)) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        // 2) 공연의 모든 회차(번호 오름차순)
        List<AmateurRounds> rounds =
                amateurRoundsRepository.findByAmateurShow_IdOrderByRoundNumberAsc(amateurShowId);

        String schedule = AmateurConverter.mergeSchedule(show.getStart(), show.getEnd());
        if (rounds.isEmpty()) {
            return ShowReservationResponseDTO.builder()
                    .showId(show.getId())
                    .showTitle(show.getName())
                    .posterImageUrl(show.getPosterImageUrl())
                    .detailAddress(show.getDetailAddress())
                    .schedule(schedule)
                    .roundSummaries(Collections.emptyList())
                    .selectedRoundId(null)
                    .selectedPerformanceDateTime(null)
                    .reservations(Collections.emptyList())
                    .build();
        }

        // 3) 공연 전체 티켓(회차 요약 계산용)

        List<ReservationStatus> statuses = Arrays.asList(
                ReservationStatus.RESERVED,
                ReservationStatus.USED
        );

        List<RealTicket> allTickets = realTicketRepository
                .findByShowTitleAndReservationStatusInOrderByIdDesc(show.getName(), statuses);
        // 관람일시 -> 티켓 목록 매핑
        Map<LocalDateTime, List<RealTicket>> ticketsByDateTime =
                allTickets.stream().collect(Collectors.groupingBy(RealTicket::getPerformanceDateTime));

        // 4) 회차 요약 생성(회차별 인원/금액 합계)
        List<ShowReservationResponseDTO.RoundSummary> summaries = new ArrayList<>();
        for (AmateurRounds r : rounds) {
            List<RealTicket> tks = ticketsByDateTime.getOrDefault(r.getPerformanceDateTime(), Collections.emptyList());
            int sumQty  = tks.stream().mapToInt(RealTicket::getQuantity).sum();
            int sumAmt  = tks.stream().mapToInt(RealTicket::getTotalPrice).sum();

            summaries.add(ShowReservationResponseDTO.RoundSummary.builder()
                    .roundId(r.getId())
                    .roundNumber(r.getRoundNumber())
                    .performanceDateTime(r.getPerformanceDateTime())
                    .sumQuantity(sumQty)
                    .sumAmount(sumAmt)
                    .build());
        }

        // 5) 선택 회차 결정
        AmateurRounds selectedRound = (roundId != null)
                ? amateurRoundsRepository.findById(roundId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ROUND_NOT_FOUND))
                : rounds.get(0);

        // 6) 선택 회차 상세(예매자 목록)
        List<RealTicket> selectedTickets =
                realTicketRepository.findByAmateurRound_IdOrderByReserveDateTimeAsc(selectedRound.getId());

        List<ShowReservationResponseDTO.ReservationRow> reservationRows =
                selectedTickets.stream()
                        .map(t -> ShowReservationResponseDTO.ReservationRow.builder()
                                // Member 표시명 필드에 맞게 수정하세요(예: getNickname, getUsername 등)
                                .reserverName(t.getMember().getUsername())
                                .quantity(t.getQuantity())
                                .reservationStatus(t.getReservationStatus())
                                .build())
                        .toList();

        // 7) 응답 조립
        return ShowReservationResponseDTO.builder()
                .showTitle(show.getName())
                .detailAddress(show.getDetailAddress())
                .roundSummaries(summaries)
                .selectedRoundId(selectedRound.getId())
                .selectedPerformanceDateTime(selectedRound.getPerformanceDateTime())
                .reservations(reservationRows)
                .build();
    }
}
