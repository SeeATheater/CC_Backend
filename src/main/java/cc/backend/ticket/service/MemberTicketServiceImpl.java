package cc.backend.ticket.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.*;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.event.entity.PromoteHotEvent;
import cc.backend.event.entity.TicketReservationEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberTicketServiceImpl implements MemberTicketService {

    private final MemberRepository memberRepository;
    private final MemberTicketRepository memberTicketRepository;
    private final AmateurShowRepository amateurShowRepository;
    private final AmateurCastingRepository amateurCastingRepository;
    private final AmateurNoticeRepository amateurNoticeRepository;
    private final AmateurTicketRepository amateurTicketRepository;
    private final AmateurStaffRepository amateurStaffRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public MemberTicketCreateResponseDTO createTicket(Long amateurRoundId, Long amateurTicketId, Member member, MemberTicketCreateRequestDTO requestDTO) {

        AmateurRounds round = amateurRoundsRepository.findById(amateurRoundId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ROUND_NOT_FOUND));

        if(requestDTO.getQuantity() > round.getTotalTicket()) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STOCK);
        }

        AmateurTicket amateurTicket = amateurTicketRepository.findById(amateurTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEUR_TICKET_NOT_FOUND));


        int totalPrice = requestDTO.getQuantity() * amateurTicket.getPrice();

        MemberTicket ticket = MemberTicket.builder()
                .member(member)
                .amateurTicket(amateurTicket)
                .amateurRound(round)
                .quantity(requestDTO.getQuantity())
                .reserveDate(LocalDateTime.now())
                .performanceDateTime(round.getPerformanceDateTime())
                .cancelAvailableUntil(round.getPerformanceDateTime().minusDays(1).withHour(17))
                .totalPrice(totalPrice)
                .reservationStatus(ReservationStatus.PENDING) // PENDING 으로
                .build();

        MemberTicket saved = memberTicketRepository.save(ticket);
//        round.decreaseTotalTicket(requestDTO.getQuantity());
//
//        amateurTicket.getAmateurShow().increaseSoldTicket(requestDTO.getQuantity());

        //티켓 예매 알림 이벤트 생성
        eventPublisher.publishEvent(new TicketReservationEvent(ticket.getAmateurTicket().getAmateurShow(), ticket.getAmateurTicket(), member));


        return MemberTicketCreateResponseDTO.builder()
                .ticketId(saved.getId())
                .showTitle(amateurTicket.getAmateurShow().getName())
                .place(amateurTicket.getAmateurShow().getPlace())
                .quantity(saved.getQuantity())
                .reserveDate(saved.getReserveDate())
                .performanceDateTime(saved.getPerformanceDateTime())
                .cancelAvailableUntil(saved.getCancelAvailableUntil())
                .totalPrice(saved.getTotalPrice())
                .reservationStatus(saved.getReservationStatus())
                .build();
    }

    @Override
    public List<MemberTicketListResponseDTO> getMyTicketList(Long memberId, String status){
        ReservationStatus filter = null;
        if (!status.equalsIgnoreCase("ALL")) {
            try {
                filter = ReservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new GeneralException(ErrorStatus.MEMBER_TICKET_WRONG_STATUS);
            }
        }

        List<MemberTicket> tickets = (filter == null)
                ? memberTicketRepository.findAllByMemberId(memberId)
                : memberTicketRepository.findAllByMemberIdAndReservationStatus(memberId, filter);

        return tickets.stream()
                .map(MemberTicketListResponseDTO::from)
                .toList();
    }

    @Override
    public MemberTicketResponseDTO getMyTicket(Long memberId, Long memberTicketId) {
        MemberTicket memberTicket = memberTicketRepository.findByMemberIdAndId(memberId, memberTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));
        return MemberTicketResponseDTO.from(memberTicket);
    }

    @Override
    @Transactional
    public MemberTicketResponseDTO cancelTicket(Long memberId, Long memberTicketId) {
        MemberTicket memberTicket = memberTicketRepository.findByMemberIdAndId(memberId, memberTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));
        if (memberTicket.getReservationStatus().equals(ReservationStatus.CANCELLED)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_ALREADY_CANCELED);
        }
        memberTicket.updateReservationStatus(ReservationStatus.CANCELLED);
        return MemberTicketResponseDTO.from(memberTicket);
    }




}
