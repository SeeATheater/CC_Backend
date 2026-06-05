package cc.backend.kakaoPay.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayResultResponseDTO;
import cc.backend.kakaoPay.service.KakaoPayBusinessService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakaoPay")
public class KakaoPayController {

    private final KakaoPayBusinessService kakaoPayBusinessService;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    // 결제 준비 요청 (결제 페이지에 대한 url 발급 요청)
    @PostMapping("/ready")
    @Operation(summary = "카카오페이 결제 준비", description = "카카오페이 결제창 URL을 발급합니다.")
    public ApiResponse<KakaoPayReadyResponseDTO> preparePayment(@RequestParam Long tempTicketId,
                                                       @AuthenticationPrincipal(expression = "member") Member member) {
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        return ApiResponse.onSuccess(kakaoPayBusinessService.preparePayment(tempTicketId, String.valueOf(member.getId())));
    }

    // 결제 승인 요청 (카카오페이 redirect 후 호출)
    @GetMapping("/approve")
    @Operation(summary = "카카오페이 결제 승인 (자동 호출)", description = "결제 완료 후 카카오 서버에서 approval_url로 자동 호출되는 API입니다. 직접 호출하지 마세요.")
    public void approve(@Parameter(description = "ticketId 입니다") @RequestParam("partner_order_id") String partnerOrderId,
                                                    @RequestParam("pg_token") String pgToken,
                                                           HttpServletResponse response) throws IOException {
        KakaoPayResultResponseDTO result =
                kakaoPayBusinessService.completePayment(partnerOrderId, pgToken);

        response.sendRedirect(
                frontendBaseUrl + "/ticketing/" + result.getAmateurShowId() + "?payment=success"
        );
    }

    // 사용자가 X버튼으로 결제 도중 취소 (환불 아님)
    @GetMapping("/cancel")
    @Operation(summary = "카카오페이 결제 중단 취소 (자동 호출)", description = "결제 중단 시 카카오 서버에서 cancel_url로 자동 호출되는 API입니다. 직접 호출하지 마세요.")
    public void cancel(@RequestParam("partner_order_id") String partnerOrderId, HttpServletResponse response) throws IOException{
        try {
            Long playId = kakaoPayBusinessService.stopPayment(partnerOrderId);
            response.sendRedirect(
                    frontendBaseUrl + "/ticketing/" + playId + "?payment=cancel"
            );
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(frontendBaseUrl);
        }
    }

    // 결제 실패 (시간 초과 15분)
    @GetMapping("/fail")
    @Operation(summary = "카카오페이 결제 실패 (자동 호출)", description = "15분간 결제 미완료 시 카카오 서버에서 fail_url로 자동 호출되는 API입니다. 직접 호출하지 마세요.")
    public void fail(@RequestParam("partner_order_id") String partnerOrderId,HttpServletResponse response) throws IOException{
        try {
            Long playId = kakaoPayBusinessService.stopPayment(partnerOrderId);
            response.sendRedirect(
                    frontendBaseUrl + "/ticketing/" + playId + "?payment=fail"
            );
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(frontendBaseUrl);
        }
    }
}
