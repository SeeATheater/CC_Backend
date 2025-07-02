package cc.backend.kakaoPay.controller;

import cc.backend.kakaoPay.dto.responseDTO.KakaoPayApproveResponseDTO;
import cc.backend.kakaoPay.dto.responseDTO.KakaoPayReadyResponseDTO;
import cc.backend.kakaoPay.service.KakaoPayService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakaoPay")
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    // 결제 준비 요청 (결제 페이지에 대한 url 발급 요청)
    @PostMapping("/ready")
    @Operation(summary = "카카오페이 결제 준비", description = "카카오페이 결제창 URL을 발급합니다.")
    public Mono<KakaoPayReadyResponseDTO> ready(@RequestParam Long ticketId,
                                                @AuthenticationPrincipal(expression = "member") Member member) {
        return kakaoPayService.ready(ticketId, String.valueOf(member.getId()));
    }

    // 결제 승인 요청 (카카오페이 redirect 후 호출)
    @GetMapping("/approve")
    @Operation(summary = "카카오페이 결제 승인", description = "카카오페이 결제 승인 요청을 처리합니다.")
    public Mono<KakaoPayApproveResponseDTO> approve(@RequestParam("partner_order_id") String partnerOrderId,
                                                    @RequestParam("pg_token") String pgToken,
                                                    @AuthenticationPrincipal(expression = "member") Member member) {
        System.out.println("🧪 member: " + member);
        return kakaoPayService.approve(partnerOrderId, pgToken, String.valueOf(member.getId()));
    }
}
