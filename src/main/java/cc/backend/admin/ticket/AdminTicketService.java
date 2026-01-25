package cc.backend.admin.ticket;

import cc.backend.admin.ticket.dto.RefundDetailResponseDTO;
import cc.backend.admin.ticket.dto.RefundListResponseDTO;
import cc.backend.admin.ticket.dto.ReservationDetailResponseDTO;
import cc.backend.admin.ticket.dto.TicketDetailResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.RealTicketRepository;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminTicketService {

    private final RealTicketRepository realTicketRepository;

    // == 소극장 티켓 관리 == //
    public Page<TicketDetailResponseDTO> getTicketList(int page, int size, String keyword) {
        Sort sort = Sort.by(
                Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RealTicket> result =
                (keyword != null && !keyword.isBlank())
                        ? realTicketRepository.findByShowTitleContainingIgnoreCase(keyword, pageable)
                        : realTicketRepository.findAll(pageable);

        List<TicketDetailResponseDTO> content = result.getContent().stream()
                .map(this::toTicketDTO)
                .toList();

        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    private TicketDetailResponseDTO toTicketDTO(RealTicket t) {
        return TicketDetailResponseDTO.builder()
                .realTicketId(t.getId())
                .showTitle(t.getShowTitle())
                .performanceDateTime(t.getPerformanceDateTime())
                .quantity(t.getQuantity())
                .build();
    }

    public TicketDetailResponseDTO getTicketDetail(Long realTicketId) {
        RealTicket ticket = realTicketRepository.findById(realTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));

        return TicketDetailResponseDTO.builder()
                .realTicketId(ticket.getId())
                .showTitle(ticket.getShowTitle())
                .performanceDateTime(ticket.getPerformanceDateTime())
                .quantity(ticket.getQuantity())
                .build();
    }

    // == 예약 내역 관리 == //
    public Page<ReservationDetailResponseDTO> getReservationList(
            int page, int size, String keyword
    ) {
        Sort sort = Sort.by(
                Sort.Order.desc("reserveDateTime"),
                Sort.Order.desc("id")
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RealTicket> result =
                (keyword != null && !keyword.isBlank())
                        ? realTicketRepository.findByShowTitleContainingIgnoreCase(keyword, pageable)
                        : realTicketRepository.findAll(pageable);

        List<ReservationDetailResponseDTO> content = result.getContent().stream()
                .map(this::toReservationDTO)
                .toList();

        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    private ReservationDetailResponseDTO toReservationDTO(RealTicket t) {
        return ReservationDetailResponseDTO.builder()
                .realTicketId(t.getId())
                .reserverName(t.getMember() != null ? t.getMember().getName() : null)
                .showTitle(t.getShowTitle())
                .performanceDateTime(t.getPerformanceDateTime())
                .detailAddress(t.getDetailAddress())
                .quantity(t.getQuantity())
                .status(t.getReservationStatus().name())
                .build();
    }

    // == 환불 내역 관리 == //

    public ReservationDetailResponseDTO getReservationDetail(Long realTicketId) {
        RealTicket ticket = realTicketRepository.findById(realTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));

        return ReservationDetailResponseDTO.builder()
                .realTicketId(ticket.getId())
                .reserverName(ticket.getMember() != null ? ticket.getMember().getName() : null)
                .showTitle(ticket.getShowTitle())
                .performanceDateTime(ticket.getPerformanceDateTime())
                .detailAddress(ticket.getDetailAddress())
                .quantity(ticket.getQuantity())
                .status(ticket.getReservationStatus().name())
                .build();
    }

    public Page<RefundListResponseDTO> getRefundList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("updatedAt"))
        );

        ReservationStatus refundStatus = ReservationStatus.CANCELLED;

        Page<RealTicket> result =
                (keyword != null && !keyword.isBlank())
                        ? realTicketRepository.findByReservationStatusAndShowTitleContainingIgnoreCase(refundStatus, keyword, pageable)
                        : realTicketRepository.findByReservationStatus(refundStatus, pageable);

        List<RefundListResponseDTO> content = result.getContent().stream()
                .map(this::toRefundDTO)
                .toList();

        return new PageImpl<>(content, pageable, result.getTotalElements());
    }

    private RefundListResponseDTO toRefundDTO(RealTicket t) {
        return RefundListResponseDTO.builder()
                .realTicketId(t.getId())
                .username(t.getMember() != null ? t.getMember().getUsername() : null)
                .memberName(t.getMember() != null ? t.getMember().getName() : null)
                .showTitle(t.getShowTitle())
                .performanceDateTime(t.getPerformanceDateTime())
                .canceledAt(t.getUpdatedAt())
                .build();
    }

    public RefundDetailResponseDTO getRefundDetail(Long realTicketId) {
        RealTicket ticket = realTicketRepository.findById(realTicketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REAL_TICKET_NOT_FOUND));



        int cancelFee = 5000; // 일단 5000원으로 고정으로하고, 환부 정책에 따라 바뀌게 할게요

        String account = ticket.getAmateurRound().getAmateurShow().getAccount();
        // 이거 맞나 싶은데....일단 이게 최선인 것 같습니다

        return RefundDetailResponseDTO.builder()
                .realTicketId(ticket.getId())
                .showTitle(ticket.getShowTitle())
                .memberName(ticket.getMember() != null ? ticket.getMember().getName() : null)
                .performanceDateTime(ticket.getPerformanceDateTime())
                .canceledAt(ticket.getUpdatedAt())
                .totalPrice(ticket.getTotalPrice())
                .cancelFee(cancelFee)
                .account(account)
                .status(ticket.getReservationStatus().name())
                .build();
    }


}
