package cc.backend.kakaoPay.dto.responseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayReadyResponseDTO {

    private String tid;

    @JsonProperty("next_redirect_pc_url")
    private String nextRedirectPcUrl; // 요청 클라이언트가 모바일 앱일 경우 redirect url

    @JsonProperty("next_redirect_mobile_url")
    private String nextRedirectMobileUrl; // 요청 클라이언트가 pc 웹일 경우 redirect url

    @JsonProperty("created_at")
    private String createdAt; // 결제 준비 요청 시간
}
