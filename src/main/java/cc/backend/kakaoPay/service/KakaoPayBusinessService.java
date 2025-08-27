package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayCancelResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.ticket.dto.response.RealTicketResponseDTO;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.CancelFeeType;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import cc.backend.ticket.repository.RealTicketRepository;
import cc.backend.ticket.service.RealTicketService;
import cc.backend.ticket.util.CancelPolicy;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoPayBusinessService {

    private final MemberTicketRepository memberTicketRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;
    private final RealTicketService realTicketService;
    private final RealTicketRepository realTicketRepository;
    private final KakaoPayService kakaoPayService;

    // 결제 준비 비즈니스 로직
    public KakaoPayReadyResponseDTO preparePayment(Long ticketId, String partnerUserId) {

        // DB에서 결제할 티켓 정보를 미리 조회
        MemberTicket memberTicket = memberTicketRepository.findWithTicketAndShowById(ticketId)
                                                          .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 현재 로그인한 사용자(partnerUserId)와 티켓의 소유주가 같은지 확인
        if (!memberTicket.getMember().getId().toString().equals(partnerUserId)) {
            throw new GeneralException(ErrorStatus.NOT_MEMBER_TICKET_OWNER);
        }

        // 재고 선점
        preemptStock(memberTicket);

        // 카카오페이 결제 준비 API 호출
        KakaoPayReadyResponseDTO responseDTO = kakaoPayService.ready(ticketId, partnerUserId);

        if (responseDTO == null) {
            throw new RuntimeException("카카오페이 결제 준비 응답을 받지 못했습니다.");
        }

        // 응답받은 tid를 DB에 저장
        memberTicket.updateTid(responseDTO.getTid());
        memberTicketRepository.save(memberTicket);

        return responseDTO;
    }

    private void preemptStock(MemberTicket memberTicket) {

        // 재고 감소
        int updated = amateurRoundsRepository.decreaseStock(memberTicket.getAmateurRound().getId(), memberTicket.getQuantity());

        if (updated == 0) { // 재고 부족하면 예외
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STOCK);
        }
    }

    // 결제 완료 비즈니스 로직
    public KakaoPayApproveResponseDTO completePayment(String partnerOrderId, String pgToken) {

        Long ticketId = Long.valueOf(partnerOrderId);

        MemberTicket memberTicket = memberTicketRepository.findWithTicketAndShowById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        if (memberTicket.getReservationStatus().equals(ReservationStatus.EXPIRED)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_EXPIRED);
        }

        // 저장된 tid 가져오기
        String tid = memberTicket.getKakaoTid();
        if (tid == null) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_TID_NOT_FOUND);
        }

        // 멤버를 DB에서 추적하기
        String partnerUserId = memberTicket.getMember().getId().toString();

        // 카카오페이 결제 승인 API 호출
        KakaoPayApproveResponseDTO responseDTO = kakaoPayService.approve(tid, partnerOrderId, partnerUserId, pgToken);

        if (responseDTO == null) {
            throw new RuntimeException("카카오페이 결제 승인 응답을 받지 못했습니다.");
        }

        // 예약 확정 및 최종 티켓 생성
        confirmReservation(memberTicket);
        realTicketService.createRealTicketFromMemberTicket(ticketId);

        return responseDTO;
    }

    private void confirmReservation(MemberTicket memberTicket) {

        // 중복 예약 방지
        if (memberTicket.getReservationStatus().equals(ReservationStatus.RESERVED)) return;

        // 상태가 PENDING이 아니면 예외 (잘못된 요청)
        if (!memberTicket.getReservationStatus().equals(ReservationStatus.PENDING)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STATUS_INVALID);
        }

        // 예약 확정
        memberTicket.updateReservationStatus(ReservationStatus.RESERVED);
        // 누적 티켓 판매 수 증가
        memberTicket.getAmateurTicket().getAmateurShow().increaseSoldTicket(memberTicket.getQuantity());
    }

    // cancel
    public RealTicketResponseDTO cancelTicket(Long memberId, Long realTicketId) {

        // 티켓 조회
        RealTicket realTicket = realTicketRepository.findByIdAndMemberId(realTicketId, memberId)
                                                    .orElseThrow(() -> new GeneralException(
                                                        ErrorStatus.REAL_TICKET_NOT_FOUND));

        // 이미 취소된 티켓인지 확인
        if (realTicket.getReservationStatus().equals(ReservationStatus.CANCELLED)) {
            throw new GeneralException(ErrorStatus.REAL_TICKET_ALREADY_CANCELED);
        }

        // 취소 가능 기한 확인
        if (realTicket.getCancelAvailableUntil().isBefore(LocalDateTime.now())) {
            throw new GeneralException(ErrorStatus.REAL_TICKET_CANCEL_NOT_AVAILABLE);
        }

        CancelFeeType cancelFeeType = CancelPolicy.determineCancelFeeType(
            realTicket.getReserveDateTime(),
            realTicket.getPerformanceDateTime(),
            LocalDateTime.now()
        );

        // 취소 금액 계산
        int cancelFee = CancelPolicy.calculateCancelFee(
            cancelFeeType,
            realTicket.getTotalPrice(),
            realTicket.getQuantity()
        );

        Integer cancelAmount = realTicket.getTotalPrice() - cancelFee;

        // 카카오페이 결제 취소 API 호출
        kakaoPayService.cancel(realTicket.getKakaoTid(), cancelAmount);

        // 취소 성공 시 티켓 상태 변경
        realTicketService.markTicketAsCancelled(realTicket);

        return RealTicketResponseDTO.from(realTicket, cancelFee, cancelAmount);
    }
}
