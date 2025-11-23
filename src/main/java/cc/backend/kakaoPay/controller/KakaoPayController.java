package cc.backend.kakaoPay.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.kakaoPay.service.KakaoPayBusinessService;
import cc.backend.kakaoPay.service.KakaoPayService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakaoPay")
public class KakaoPayController {

    private final KakaoPayBusinessService kakaoPayBusinessService;

    // 결제 준비 요청 (결제 페이지에 대한 url 발급 요청)
    @PostMapping("/ready")
    @Operation(summary = "카카오페이 결제 준비", description = "카카오페이 결제창 URL을 발급합니다.")
    public ApiResponse<KakaoPayReadyResponseDTO> preparePayment(@RequestParam Long memberTicketId,
                                                       @AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(kakaoPayBusinessService.preparePayment(memberTicketId, String.valueOf(member.getId())));
    }

    // 결제 승인 요청 (카카오페이 redirect 후 호출)
    @GetMapping("/approve")
    @Operation(summary = "카카오페이 결제 승인 (자동 호출)", description = "결제 완료 후 카카오 서버에서 approval_url로 자동 호출되는 API입니다. 직접 호출하지 마세요.")
    public ApiResponse<KakaoPayApproveResponseDTO> approve(@Parameter(description = "ticketId 입니다") @RequestParam("partner_order_id") String partnerOrderId,
                                                    @RequestParam("pg_token") String pgToken) {

        return ApiResponse.onSuccess(kakaoPayBusinessService.completePayment(partnerOrderId, pgToken));
    }

    // 사용자가 X버튼으로 결제 도중 취소 (환불 아님)
    @GetMapping("/cancel")
    @Operation(summary = "카카오페이 결제 승인 (자동 호출)", description = "결제 중단 시 카카오 서버에서 cancel_url로 자동 호출되는 API입니다. 직접 호출하지 마세요.")
    public ApiResponse<String> cancel(@RequestParam("partner_order_id") String partnerOrderId) {
        kakaoPayBusinessService.stopPayment(partnerOrderId);
        return ApiResponse.onSuccess("결제가 취소되었습니다. (재고 복구 완료)");
    }

    // 결제 실패 (시간 초과 15분)
    @GetMapping("/fail")
    @Operation(summary = "카카오페이 결제 승인 (자동 호출)", description = "15분간 결제 미완료 시 카카오 서버에서 fail_url로 자동 호출되는 API입니다. 직접 호출하지 마세요.")
    public ApiResponse<String> fail(@RequestParam("partner_order_id") String partnerOrderId) {
        kakaoPayBusinessService.stopPayment(partnerOrderId);
        return ApiResponse.onSuccess("결제에 실패했습니다. (재고 복구 완료)");
    }
}
