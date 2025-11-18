package cc.backend.ticket.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.AmateurCastingRepository;
import cc.backend.amateurShow.repository.AmateurNoticeRepository;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.AmateurStaffRepository;
import cc.backend.amateurShow.repository.AmateurTicketRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.event.entity.TicketReservationEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.AmateurShowSimpleDTO;
import cc.backend.ticket.dto.response.AmateurTicketListDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.RoundsListDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTicketServiceImpl implements MemberTicketService {

    private final MemberTicketRepository memberTicketRepository;
    private final AmateurShowRepository amateurShowRepository;
    private final AmateurTicketRepository amateurTicketRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RealTicketService realTicketService;
    private final MemberRepository memberRepository;


    @Override
    @Transactional
    public MemberTicketCreateResponseDTO createTicket(Long amateurShowId, Long amateurRoundId, Long amateurTicketId, Member member, MemberTicketCreateRequestDTO requestDTO) {

        Member memberRef = memberRepository.getReferenceById(member.getId());
        AmateurShow show = amateurShowRepository.findById(amateurShowId)
                .orElseThrow(()-> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        AmateurRounds round = amateurRoundsRepository.findById(amateurRoundId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ROUND_NOT_FOUND));

        AmateurTicket amateurTicket = amateurTicketRepository.findById(amateurTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEUR_TICKET_NOT_FOUND));

        // 해당 회차와 티켓에 해당하는 공연이 일치 하는지
        if (!amateurTicket.getAmateurShow().getId().equals(amateurShowId) || !round.getAmateurShow().getId().equals(amateurShowId)) {
            throw new GeneralException(ErrorStatus.AMATEUR_SHOW_MISMATCH);
        }

        // 현재 예약하려는 티켓의 수량이 해당 회차의 재고수량을 초과하지 않는지
        if(requestDTO.getQuantity() > round.getTotalTicket()) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STOCK);
        }

        int totalPrice = requestDTO.getQuantity() * amateurTicket.getPrice();
        String bookingNumber = generateBookingNumber();

        MemberTicket ticket = MemberTicket.builder()
                .member(memberRef)
                .amateurTicket(amateurTicket)
                .amateurRound(round)
                .quantity(requestDTO.getQuantity())
                .reserveDate(LocalDateTime.now())
                .bookingNumber(bookingNumber)
                .performanceDateTime(round.getPerformanceDateTime())
                .cancelAvailableUntil(round.getPerformanceDateTime().minusDays(1).withHour(17))
                .totalPrice(totalPrice)
                .reservationStatus(ReservationStatus.PENDING)
                .build();

        MemberTicket saved = memberTicketRepository.save(ticket);

        //티켓 예매 알림 이벤트 생성
        eventPublisher.publishEvent(new TicketReservationEvent(ticket.getAmateurTicket().getAmateurShow(), ticket.getAmateurTicket(), memberRef));


        realTicketService.createRealTicketFromMemberTicket(saved.getId());
        return MemberTicketCreateResponseDTO.builder()
                .memberTicketId(saved.getId())
                .bookingNumber(bookingNumber)
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
                .posterKeyName(amateurShow.getPosterKeyName())
                .build();
    }


    private String generateBookingNumber() {
        String prefix = "TICKET";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = new Random().nextInt(9000) + 1000; // 1000~9999
        return  prefix + timestamp + randomNum;
    }
}
