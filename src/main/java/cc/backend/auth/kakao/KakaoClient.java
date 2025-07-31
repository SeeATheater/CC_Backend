package cc.backend.auth.kakao;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.auth.kakao.dto.response.KakaoTokenResponse;
import cc.backend.auth.kakao.dto.response.KakaoUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class KakaoClient {

    private final WebClient webClient;
    private final KakaoProperties kakaoProperties;

    public KakaoClient(KakaoProperties kakaoProperties) {
        this.kakaoProperties = kakaoProperties;
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    public KakaoTokenResponse getAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProperties.getClientId());

        if (kakaoProperties.getClientSecret() != null &&
                !kakaoProperties.getClientSecret().isEmpty()) {
            params.add("client_secret", kakaoProperties.getClientSecret());
        }

        params.add("redirect_uri", kakaoProperties.getRedirectUri());
        params.add("code", authorizationCode);


        try {
            return webClient.post()
                    .uri(kakaoProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("카카오 토큰 요청 실패: {}", response.statusCode());
                        return Mono.error(new GeneralException(ErrorStatus.KAKAO_TOKEN_REQUEST_FAILED));
                    })
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("카카오 토큰 요청 중 오류 발생", e);
            throw new GeneralException(ErrorStatus.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri(kakaoProperties.getUserInfoUri())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("카카오 사용자 정보 요청 실패: {}", response.statusCode());
                        return Mono.error(new GeneralException(ErrorStatus.KAKAO_USER_INFO_REQUEST_FAILED));
                    })
                    .bodyToMono(KakaoUserInfo.class)
                    .block();
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 중 오류 발생", e);
            throw new GeneralException(ErrorStatus.KAKAO_USER_INFO_REQUEST_FAILED);
        }
    }
}
