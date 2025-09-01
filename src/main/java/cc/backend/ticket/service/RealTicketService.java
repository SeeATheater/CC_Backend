package cc.backend.ticket.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.service.KakaoPayService;
import cc.backend.ticket.dto.response.RealTicketResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.CancelFeeType;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import cc.backend.ticket.repository.RealTicketRepository;
import cc.backend.ticket.util.CancelPolicy;
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
    private final AmateurRoundsRepository amateurRoundsRepository;


    @Transactional
    public void createRealTicketFromMemberTicket(Long  memberTicketId) {
        MemberTicket ticket = memberTicketRepository.findById(memberTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        AmateurRounds round = ticket.getAmateurRound();

        // 취소 수수료 정책 적용
        CancelFeeType cancelFeeType = CancelPolicy.determineCancelFeeType(
            ticket.getReserveDate(),
            ticket.getPerformanceDateTime(),
            LocalDateTime.now()
        );

        String cancelPolicyText = CancelPolicy.getCancelFeePolicyText(cancelFeeType);

        RealTicket realTicket = RealTicket.builder()
                .member(ticket.getMember())
                .amateurRound(round)
                .showTitle(ticket.getAmateurTicket().getAmateurShow().getName())
                .posterImageUrl(ticket.getAmateurTicket().getAmateurShow().getPosterImageUrl())
                .detailAddress(ticket.getAmateurTicket().getAmateurShow().getDetailAddress())
                .performanceDateTime(ticket.getPerformanceDateTime())
                .reserveDateTime(ticket.getReserveDate())
                .quantity(ticket.getQuantity())
                .totalPrice(ticket.getTotalPrice())
                .reservationStatus(ticket.getReservationStatus())
                .cancelAvailableUntil(ticket.getCancelAvailableUntil())
                .cancelFeePolicyText(cancelPolicyText)
                .kakaoTid(ticket.getKakaoTid())
                .build();

        realTicketRepository.save(realTicket);
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
        RealTicket realTicket = realTicketRepository.findByIdAndMemberId(realTicketId, memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));
        return RealTicketResponseDTO.from(realTicket);
    }

    @Transactional
    public void markTicketAsCancelled(RealTicket realTicket) {

        //해당 회차 재고 복구
        amateurRoundsRepository.increaseStock(realTicket.getAmateurRound().getId(), realTicket.getQuantity());

        // 누적 티켓 판매수 복구
        realTicket.getAmateurRound().getAmateurShow().decreaseSoldTicket(realTicket.getQuantity());

        realTicket.updateReservationStatus(ReservationStatus.CANCELLED);
    }


}
