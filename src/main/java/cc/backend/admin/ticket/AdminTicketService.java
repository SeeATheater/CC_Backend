package cc.backend.admin.ticket;

import cc.backend.admin.ticket.dto.ReservationListResponseDTO;
import cc.backend.ticket.entity.RealTicket;
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

    public Slice<ReservationListResponseDTO> getReservationList(
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

        List<ReservationListResponseDTO> content = result.getContent().stream()
                .map(this::toDto)
                .toList();

        return new SliceImpl<>(content, pageable, result.hasNext());
    }

    private ReservationListResponseDTO toDto(RealTicket t) {
        return ReservationListResponseDTO.builder()
                .realTicketId(t.getId())
                .reserverName(t.getMember() != null ? t.getMember().getName() : null)
                .showTitle(t.getShowTitle())
                .performanceDateTime(t.getPerformanceDateTime())
                .detailAddress(t.getDetailAddress())
                .quantity(t.getQuantity())
                .status(t.getReservationStatus().name())
                .build();
    }
}
