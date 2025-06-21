package cc.backend.ticket.service;

import java.time.LocalDateTime;
import java.util.List;

import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.*;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
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
    private final AmateurSummaryRepository amateurSummaryRepository;
    private final AmateurStaffRepository amateurStaffRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;


    @Override
    @Transactional
    public MemberTicketCreateResponseDTO create(Long amateurTicketId, MemberTicketCreateRequestDTO requestDTO) {
        Member member = memberRepository.findById(requestDTO.getMemberId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        AmateurTicket amateurTicket = amateurTicketRepository.findById(amateurTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        int totalPrice = requestDTO.getQuantity() * amateurTicket.getPrice();

        MemberTicket ticket = MemberTicket.builder()
                .member(member)
                .amateurTicket(amateurTicket)
                .quantity(requestDTO.getQuantity())
                .reserveDate(LocalDateTime.now())
                .performanceDateTime(requestDTO.getPerformanceDateTime())
                .cancelAvailableUntil(requestDTO.getPerformanceDateTime().minusDays(1).withHour(17))
                .totalPrice(totalPrice)
                .reservationStatus(ReservationStatus.RESERVED)
                .build();

        MemberTicket saved = memberTicketRepository.save(ticket);

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
    public MemberTicketResponseDTO getMyTicket(Long memberId, Long ticketId) {
        MemberTicket memberTicket = memberTicketRepository.findByMemberIdAndId(memberId, ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));
        return MemberTicketResponseDTO.from(memberTicket);
    }

    @Override
    @Transactional
    public MemberTicketResponseDTO cancelTicket(Long memberId, Long ticketId) {
        MemberTicket memberTicket = memberTicketRepository.findByMemberIdAndId(memberId, ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));
        if (memberTicket.getReservationStatus().equals(ReservationStatus.CANCELLED)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_ALREADY_CANCELED);
        }
        memberTicket.updateReservationStatus(ReservationStatus.CANCELLED);
        return MemberTicketResponseDTO.from(memberTicket);
    }




}
