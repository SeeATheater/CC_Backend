package cc.backend.performer;


import cc.backend.amateurShow.converter.AmateurConverter;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import cc.backend.performer.dto.PerformerEnrolledShowResponseDTO;
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
    private final MemberRepository  memberRepository;
/*    public Slice<PerformerMyShowResponseDTO> getMyShows(Long memberId, String tab, Pageable pageable) {

        Slice<AmateurShow> slice;

        if ("on_sale".equalsIgnoreCase(tab)) { // 예매 진행
            slice = amateurShowRepository.findByMember_IdAndStatusInOrderByIdDesc(
                    memberId,
                    EnumSet.of(AmateurShowStatus.APPROVED_ONGOING, AmateurShowStatus.APPROVED_YET),
                    pageable
            );
        } else if ("ended".equalsIgnoreCase(tab)) { // 공연 종료
            slice = amateurShowRepository.findByMember_IdAndStatusInOrderByIdDesc(
                    memberId,
                    EnumSet.of(AmateurShowStatus.APPROVED_ENDED),
                    pageable
            );
        } else { // 전체
            slice = amateurShowRepository.findByMember_IdOrderByIdDesc(memberId, pageable);
        }

        return slice.map(PerformerMyShowResponseDTO::from);
        }*/

    public PerformerEnrolledShowResponseDTO.MyEnrolledAmateurShowList getMyAmateurShow(Long memberId, AmateurShowStatus status, Pageable pageable) {
        // 멤버 여기서 뽑고
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        // 공연자인지 한번 더 검사
        if (member.getRole() != Role.PERFORMER) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_PERFORMER);
        }
        // 여기서 슬라이싱으로 공연들 뽑아내고
        Slice<AmateurShow> slice;
        if (status == null) {
            slice = amateurShowRepository.findAllByMemberIdOrderByIdDesc(memberId, pageable);
        } else {
            slice = amateurShowRepository.findAllByMemberIdAndStatusOrderByIdDesc(memberId, status, pageable);
        }

        return AmateurConverter.toMyEnrolledAmateurShowList(slice);
    }
    public ShowReservationResponseDTO getShowReservationList(Long amateurShowId, Long roundId) {

        // 1) 공연 로드
        AmateurShow show = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

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
