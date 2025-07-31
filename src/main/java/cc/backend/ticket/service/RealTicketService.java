package cc.backend.ticket.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
import cc.backend.ticket.dto.response.RealTicketResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import cc.backend.ticket.repository.RealTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealTicketService {
    private final RealTicketRepository realTicketRepository;
    private final MemberTicketRepository memberTicketRepository;


    public void createRealTicketFromMemberTicket(Long  memberTicketId) {
        MemberTicket ticket = memberTicketRepository.findById(memberTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        RealTicket realTicket = RealTicket.builder()
                .member(ticket.getMember())
                .showTitle(ticket.getAmateurTicket().getAmateurShow().getName())
                .posterImageUrl(ticket.getAmateurTicket().getAmateurShow().getPosterImageUrl())
                .place(ticket.getAmateurTicket().getAmateurShow().getPlace())
                .performanceDateTime(ticket.getPerformanceDateTime())
                .reserveDateTime(ticket.getReserveDate())
                .quantity(ticket.getQuantity())
                .totalPrice(ticket.getTotalPrice())
                .reservationStatus(ticket.getReservationStatus())
                .cancelAvailableUntil(ticket.getCancelAvailableUntil())
                .cancelFeePolicyText(getCancelFeePolicyText(ticket.getPerformanceDateTime()))
                .build();

        realTicketRepository.save(realTicket);
    }

    // 정적 문자열로 예시. 추후 정책 변경 시 동적으로 만들어도 됨.
    private String getCancelFeePolicyText(LocalDateTime performanceDateTime) {
        return "일단 임시로";
    }

    public List<RealTicketResponseDTO> getMyTicketList(Long memberId, String status) {
        List<RealTicket> realTickets;

        if ("ALL".equalsIgnoreCase(status)) {
            realTickets = realTicketRepository.findAllByMemberId(memberId);
        } else {
            ReservationStatus parsedStatus;
            try {
                parsedStatus = ReservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new GeneralException(ErrorStatus.INVALID_RESERVATION_STATUS);
            }
            realTickets = realTicketRepository.findAllByMemberIdAndReservationStatus(memberId, parsedStatus);
        }

        return realTickets.stream()
                .map(RealTicketResponseDTO::from)
                .toList();
    }

    public RealTicketResponseDTO getMyTicket(Long memberId, Long realTicketId) {
        RealTicket realTicket = realTicketRepository.findByIdAndMemberId(memberId, realTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));
        return RealTicketResponseDTO.from(realTicket);
    }

    @Transactional
    public RealTicketResponseDTO cancelTicket(Long memberId, Long realTicketId) {
        RealTicket realTicket = realTicketRepository.findByIdAndMemberId(memberId, realTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));
        if (realTicket.getReservationStatus().equals(ReservationStatus.CANCELLED)) {
            throw new GeneralException(ErrorStatus.REAL_TICKET_ALREADY_CANCELED);
        }
        realTicket.updateReservationStatus(ReservationStatus.CANCELLED);
        return RealTicketResponseDTO.from(realTicket);
    }


}
