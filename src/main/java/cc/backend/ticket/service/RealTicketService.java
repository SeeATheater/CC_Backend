package cc.backend.ticket.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.ticket.dto.response.RealTicketResponseDTO;
import cc.backend.ticket.dto.response.ShowSnapshot;
import cc.backend.ticket.entity.TempTicket;
import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.CancelFeeType;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.TempTicketRepository;
import cc.backend.ticket.repository.RealTicketRepository;
import cc.backend.ticket.util.CancelPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RealTicketService {
    private final RealTicketRepository realTicketRepository;
    private final TempTicketRepository tempTicketRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;


    @Transactional
    public void createRealTicketFromTempTicket(TempTicket tempTicket) {

        AmateurRounds round = tempTicket.getAmateurRound();

        // 취소 수수료 정책 적용
        CancelFeeType cancelFeeType = CancelPolicy.determineCancelFeeType(
            tempTicket.getReserveDate(),
            tempTicket.getPerformanceDateTime(),
            LocalDateTime.now()
        );

        ShowSnapshot show = tempTicket.extractShowSnapshot();


        String cancelPolicyText = CancelPolicy.getCancelFeePolicyText(cancelFeeType);

        RealTicket realTicket = RealTicket.builder()
                .member(tempTicket.getMember())
                .amateurRound(round)
                .showTitle(show.title())
                .posterImageUrl(show.posterImageUrl())
                .detailAddress(show.detailAddress())
                .performanceDateTime(tempTicket.getPerformanceDateTime())
                .reserveDateTime(tempTicket.getReserveDate())
                .quantity(tempTicket.getQuantity())
                .totalPrice(tempTicket.getTotalPrice())
                .reservationStatus(tempTicket.getReservationStatus())
                .cancelAvailableUntil(tempTicket.getCancelAvailableUntil())
                .cancelFeePolicyText(cancelPolicyText)
                .kakaoTid(tempTicket.getKakaoTid())
                .build();

        realTicketRepository.save(realTicket);
    }

    public Slice<RealTicketResponseDTO> getMyTicketList(Long memberId, String status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Slice<RealTicket> realTickets;
        if ("ALL".equalsIgnoreCase(status)) {
            realTickets = realTicketRepository.findAllByMemberId(memberId, pageable);
        } else {
            ReservationStatus parsedStatus;
            try {
                parsedStatus = ReservationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new GeneralException(ErrorStatus.INVALID_RESERVATION_STATUS);
            }
            realTickets = realTicketRepository.findAllByMemberIdAndReservationStatus(memberId, parsedStatus, pageable);
        }

        return realTickets.map(RealTicketResponseDTO::from);
    }

    public RealTicketResponseDTO getMyTicket(Long memberId, Long realTicketId) {
        RealTicket realTicket = realTicketRepository.findById(realTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));
        if(!(realTicket.getMember().getId()).equals(memberId)){
            throw new GeneralException(ErrorStatus.NOT_REAL_TICKET_OWNER);
        }
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
