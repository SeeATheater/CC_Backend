package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayResultResponseDTO;
import cc.backend.member.entity.Member;
import cc.backend.ticket.entity.TempTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.RealTicketRepository;
import cc.backend.ticket.repository.TempTicketRepository;
import cc.backend.ticket.service.RealTicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KakaoPayBusinessServiceTest {

    @Mock
    private TempTicketRepository tempTicketRepository;

    @Mock
    private AmateurRoundsRepository amateurRoundsRepository;

    @Mock
    private RealTicketService realTicketService;

    @Mock
    private RealTicketRepository realTicketRepository;

    @Mock
    private KakaoPayService kakaoPayService;

    @InjectMocks
    private KakaoPayBusinessService kakaoPayBusinessService;

    @Test
    void completePayment_createsRealTicketOnFirstApproval() {
        TempTicket tempTicket = createTempTicket(ReservationStatus.PENDING);
        KakaoPayApproveResponseDTO approveResponse = KakaoPayApproveResponseDTO.builder()
                .tid("tid-1")
                .partnerOrderId("1")
                .partnerUserId("10")
                .build();

        when(tempTicketRepository.findWithTicketAndShowById(1L)).thenReturn(Optional.of(tempTicket));
        when(realTicketRepository.existsByKakaoTid("tid-1")).thenReturn(false);
        when(kakaoPayService.approve("tid-1", "1", "10", "pg-token")).thenReturn(approveResponse);

        KakaoPayResultResponseDTO result = kakaoPayBusinessService.completePayment("1", "pg-token");

        assertEquals(100L, result.getAmateurShowId());
        assertEquals(approveResponse, result.getApproveResponse());
        assertEquals(ReservationStatus.RESERVED, tempTicket.getReservationStatus());
        verify(kakaoPayService).approve("tid-1", "1", "10", "pg-token");
        verify(realTicketService).createRealTicketFromTempTicket(tempTicket);
    }

    @Test
    void completePayment_doesNotCreateRealTicketWhenApprovalIsRetried() {
        TempTicket tempTicket = createTempTicket(ReservationStatus.RESERVED);

        when(tempTicketRepository.findWithTicketAndShowById(1L)).thenReturn(Optional.of(tempTicket));
        when(realTicketRepository.existsByKakaoTid("tid-1")).thenReturn(true);

        KakaoPayResultResponseDTO result = kakaoPayBusinessService.completePayment("1", "pg-token");

        assertEquals(100L, result.getAmateurShowId());
        assertNull(result.getApproveResponse());
        verify(kakaoPayService, never()).approve(any(), any(), any(), any());
        verify(realTicketService, never()).createRealTicketFromTempTicket(any());
    }

    @Test
    void completePayment_doesNotCreateRealTicketWhenRealTicketAlreadyExists() {
        TempTicket tempTicket = createTempTicket(ReservationStatus.PENDING);

        when(tempTicketRepository.findWithTicketAndShowById(1L)).thenReturn(Optional.of(tempTicket));
        when(realTicketRepository.existsByKakaoTid("tid-1")).thenReturn(true);

        KakaoPayResultResponseDTO result = kakaoPayBusinessService.completePayment("1", "pg-token");

        assertEquals(100L, result.getAmateurShowId());
        assertNull(result.getApproveResponse());
        verify(kakaoPayService, never()).approve(any(), any(), any(), any());
        verify(realTicketService, never()).createRealTicketFromTempTicket(any());
    }

    @Test
    void preparePayment_throwsWhenKakaoTidAlreadyExists() {
        TempTicket tempTicket = createTempTicket(ReservationStatus.PENDING);

        when(tempTicketRepository.findWithTicketAndShowById(1L)).thenReturn(Optional.of(tempTicket));

        GeneralException exception = assertThrows(GeneralException.class,
                () -> kakaoPayBusinessService.preparePayment(1L, "10"));

        assertSame(ErrorStatus.TEMP_TICKET_STATUS_INVALID, exception.getCode());
        verify(amateurRoundsRepository, never()).decreaseStock(anyLong(), anyInt());
        verify(kakaoPayService, never()).ready(anyLong(), anyString());
    }

    private TempTicket createTempTicket(ReservationStatus status) {
        Member member = mock(Member.class);
        lenient().when(member.getId()).thenReturn(10L);

        AmateurShow show = AmateurShow.builder()
                .id(100L)
                .name("show")
                .detailAddress("address")
                .build();

        AmateurTicket ticket = AmateurTicket.builder()
                .amateurShow(show)
                .discountName("regular")
                .price(10000)
                .build();

        AmateurRounds round = AmateurRounds.builder()
                .amateurShow(show)
                .performanceDateTime(LocalDateTime.now().plusDays(1))
                .build();

        return TempTicket.builder()
                .quantity(1)
                .reservationStatus(status)
                .reserveDate(LocalDateTime.now())
                .performanceDateTime(LocalDateTime.now().plusDays(1))
                .cancelAvailableUntil(LocalDateTime.now().plusHours(1))
                .totalPrice(10000)
                .amateurTicket(ticket)
                .member(member)
                .amateurRound(round)
                .kakaoTid("tid-1")
                .build();
    }
}
