package cc.backend.notice.unit.admin;

import cc.backend.admin.amateurShow.service.AdminApprovalService;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.service.OutboxService;
import cc.backend.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 공연 승인 서비스의 비즈니스 분기와 Outbox 생성 요청을 검증하는 단위 테스트.
 *
 * <p>AdminApprovalService만 실제 인스턴스이며 Repository, OutboxService,
 * 엔티티는 Mock이다. 따라서 DB 저장이나 Kafka 발행은 이 테스트의 범위가 아니다.</p>
 * <pre>
 * 시나리오 1: WAITING 공연 승인
 *     -> 공연 상태 변경 요청
 *     -> eventId가 포함된 ApprovalShowEvent 생성
 *     -> approval-show-topic Outbox 저장 요청
 *
 * 시나리오 2: 이미 APPROVED인 공연 재승인
 *     -> AMATEURSHOW_ALREADY_APPROVED 예외
 *     -> 상태 변경과 Outbox 저장 요청 모두 발생하지 않음
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class AdminApprovalServiceTest {

    @Mock
    private AmateurShowRepository amateurShowRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private AmateurShow show;

    @Mock
    private Member performer;

    @InjectMocks
    private AdminApprovalService adminApprovalService;

    @Test
    void storesApprovalEventInOutboxWhenShowIsApproved() {
        when(amateurShowRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(show));
        when(show.getApprovalStatus()).thenReturn(ApprovalStatus.WAITING);
        when(show.getId()).thenReturn(1L);
        when(show.getMember()).thenReturn(performer);
        when(performer.getId()).thenReturn(2L);

        adminApprovalService.approveShow(1L);

        ArgumentCaptor<ApprovalShowEvent> eventCaptor =
                ArgumentCaptor.forClass(ApprovalShowEvent.class);
        verify(show).approve();
        verify(outboxService).appendOutboxEvent(
                eventCaptor.capture(),
                eq("approval-show-topic"),
                eq("1")
        );
        assertThat(eventCaptor.getValue().amateurShowId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().performerId()).isEqualTo(2L);
        assertThat(eventCaptor.getValue().eventId()).isNotBlank();
    }

    @Test
    void rejectsAlreadyApprovedShowWithoutCreatingAnotherOutboxEvent() {
        when(amateurShowRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(show));
        when(show.getApprovalStatus()).thenReturn(ApprovalStatus.APPROVED);

        assertThatThrownBy(() -> adminApprovalService.approveShow(1L))
                .isInstanceOfSatisfying(GeneralException.class, exception ->
                        assertThat(exception.getCode())
                                .isEqualTo(ErrorStatus.AMATEURSHOW_ALREADY_APPROVED)
                );

        verify(show, never()).approve();
        verify(outboxService, never()).appendOutboxEvent(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }
}
