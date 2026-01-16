package cc.backend.ticket.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.AmateurTicketRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.notice.event.TicketReservationCommitEvent;
import cc.backend.kafka.event.reservationCompletedEvent.ReservationCompletedProducer;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.ticket.dto.request.TempTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.AmateurShowSimpleDTO;
import cc.backend.ticket.dto.response.AmateurTicketListDTO;
import cc.backend.ticket.dto.response.TempTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.RoundsListDTO;
import cc.backend.ticket.entity.TempTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.TempTicketRepository;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TempTicketServiceImpl implements TempTicketService {

    private final TempTicketRepository tempTicketRepository;
    private final AmateurShowRepository amateurShowRepository;
    private final AmateurTicketRepository amateurTicketRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RealTicketService realTicketService;
    private final MemberRepository memberRepository;
    private final ReservationCompletedProducer reservationCompletedProducer;


    @Override
    @Transactional
    public TempTicketCreateResponseDTO createTempTicket(Long amateurShowId, Long amateurRoundId, Long amateurTicketId, Member member, TempTicketCreateRequestDTO requestDTO) {

        Member memberRef = memberRepository.getReferenceById(member.getId());
        AmateurShow show = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        AmateurRounds round = amateurRoundsRepository.findById(amateurRoundId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ROUND_NOT_FOUND));

        AmateurTicket amateurTicket = amateurTicketRepository.findById(amateurTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEUR_TICKET_NOT_FOUND));

        // 해당 회차의 예약 가능한 시간인지
        // 예매 가능 기한(공연 시작 3시간 전) 체크
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingDeadline = round.getPerformanceDateTime().minusHours(3);

        if (now.isAfter(bookingDeadline)) {
            throw new GeneralException(ErrorStatus.ROUND_BOOKING_DEADLINE_PASSED);
        }

        // 해당 회차와 티켓에 해당하는 공연이 일치 하는지
        if (!amateurTicket.getAmateurShow().getId().equals(amateurShowId) || !round.getAmateurShow().getId().equals(amateurShowId)) {
            throw new GeneralException(ErrorStatus.AMATEUR_SHOW_MISMATCH);
        }

        // 현재 예약하려는 티켓의 수량이 해당 회차의 재고수량을 초과하지 않는지
        if(requestDTO.getQuantity() > round.getTotalTicket()) {
            throw new GeneralException(ErrorStatus.TEMP_TICKET_STOCK);
        }

        int totalPrice = requestDTO.getQuantity() * amateurTicket.getPrice();

        TempTicket ticket = TempTicket.builder()
                .member(memberRef)
                .amateurTicket(amateurTicket)
                .amateurRound(round)
                .quantity(requestDTO.getQuantity())
                .reserveDate(LocalDateTime.now())
                .performanceDateTime(round.getPerformanceDateTime())
                .cancelAvailableUntil(round.getPerformanceDateTime().minusDays(1).withHour(17))
                .totalPrice(totalPrice)
                .reservationStatus(ReservationStatus.PENDING)
                .build();

        TempTicket saved = tempTicketRepository.save(ticket);

        // -> 먼저 ApplicationEvent를 완충 이벤트로 커밋 이후를 보장받고 나서 카프카 이벤트 발행
        eventPublisher.publishEvent(
                new TicketReservationCommitEvent(ticket.getAmateurTicket().getAmateurShow().getId(), ticket.getAmateurTicket().getId(), memberRef.getId())
        );

        // realTicket은 API를 사용해 호출

        return TempTicketCreateResponseDTO.builder()
                .tempTicketId(saved.getId())
                .showTitle(amateurTicket.getAmateurShow().getName())
                .detailAddress(amateurTicket.getAmateurShow().getDetailAddress())
                .quantity(saved.getQuantity())
                .reserveDate(saved.getReserveDate())
                .performanceDateTime(saved.getPerformanceDateTime())
                .cancelAvailableUntil(saved.getCancelAvailableUntil())
                .totalPrice(saved.getTotalPrice())
                .reservationStatus(saved.getReservationStatus())
                .build();
    }

    @Override
    public List<RoundsListDTO> getRoundsList(Long memberId, Long amateurShowId){
        List<AmateurRounds> rounds = amateurRoundsRepository.findByAmateurShowId(amateurShowId);
        return rounds.stream()
                .map(r -> new RoundsListDTO(
                        r.getId(),
                        r.getRoundNumber(),
                        r.getPerformanceDateTime()
                )).toList();
    }

    @Override
    public List<AmateurTicketListDTO> getAmateurTicketList(Long memberId, Long amateurShowId){
        List<AmateurTicket> tickets = amateurTicketRepository.findByAmateurShowId(amateurShowId);
        return tickets.stream()
                .map(t -> new AmateurTicketListDTO(
                        t.getId(),
                        t.getDiscountName(),
                        t.getPrice()
                )).toList();
    }

    @Override
    public AmateurShowSimpleDTO getSimpleAmateurShow(Long amateurShowId){
        AmateurShow amateurShow = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));
        return AmateurShowSimpleDTO.builder()
                .amateurShowId(amateurShowId)
                .name(amateurShow.getName())
                //.place(amateurShow.getPlace())
                .detailAddress(amateurShow.getDetailAddress())
                .posterImageUrl(amateurShow.getPosterImageUrl())
                .build();
    }


}
