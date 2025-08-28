package cc.backend.kakaoPay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

// -- Request Syntax--
//POST /online/v1/payment/ready HTTP/1.1
//Host: open-api.kakaopay.com
//        Authorization: SECRET_KEY ${SECRET_KEY}
//        Content-Type: application/json

@Configuration
public class KakaoPayConfig {

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.base-url}")
    private String baseUrl;

    @Bean
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl) // host
                .defaultHeader(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + secretKey) // authorization
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // content-type
                .build();
    }
}
