package cc.backend.ticket.service;

import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.AmateurTicketRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.ticket.dto.request.TempTicketCreateRequestDTO;
import cc.backend.ticket.repository.TempTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TempTicketServiceImplTest {

    @Mock
    private TempTicketRepository tempTicketRepository;

    @Mock
    private AmateurShowRepository amateurShowRepository;

    @Mock
    private AmateurTicketRepository amateurTicketRepository;

    @Mock
    private AmateurRoundsRepository amateurRoundsRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RealTicketService realTicketService;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TempTicketServiceImpl tempTicketService;

    @Test
    void createTempTicket_rejectsZeroQuantity() {
        assertInvalidQuantityRejected(0);
    }

    @Test
    void createTempTicket_rejectsNegativeQuantity() {
        assertInvalidQuantityRejected(-1);
    }

    @Test
    void createTempTicket_rejectsNullRequest() {
        Member member = mock(Member.class);

        GeneralException exception = assertThrows(GeneralException.class,
                () -> tempTicketService.createTempTicket(1L, 1L, 1L, member, null));

        assertSame(ErrorStatus.TEMP_TICKET_QUANTITY, exception.getCode());
        verifyNoRepositoryOrEventInteractions();
    }

    private void assertInvalidQuantityRejected(int quantity) {
        TempTicketCreateRequestDTO requestDTO = TempTicketCreateRequestDTO.builder()
                .quantity(quantity)
                .build();
        Member member = mock(Member.class);

        GeneralException exception = assertThrows(GeneralException.class,
                () -> tempTicketService.createTempTicket(1L, 1L, 1L, member, requestDTO));

        assertSame(ErrorStatus.TEMP_TICKET_QUANTITY, exception.getCode());
        verifyNoRepositoryOrEventInteractions();
    }

    private void verifyNoRepositoryOrEventInteractions() {
        verifyNoInteractions(
                memberRepository,
                amateurShowRepository,
                amateurRoundsRepository,
                amateurTicketRepository,
                tempTicketRepository,
                eventPublisher,
                realTicketService
        );
    }
}
