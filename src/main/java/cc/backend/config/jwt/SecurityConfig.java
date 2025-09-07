package cc.backend.config.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowCredentials(true);
                    // 실제 배포 환경 도메인
                    config.addAllowedOrigin("https://seeatheater.site");
                    config.addAllowedOrigin("https://www.seeatheater.site");
                    config.addAllowedOrigin("https://api.seeatheater.site");

                    // 개발 환경
                    config.addAllowedOrigin("http://localhost:8080"); // '*' 대신 명시적 출처 사용
                    config.addAllowedOrigin("http://localhost:5173"); // '*' 대신 명시적 출처 사용
                    //config.addAllowedOrigin("*");
                    config.addAllowedHeader("*");
                    config.addAllowedMethod("*");
                    return config;
                }))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()  // 헬스체크 허용
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/kakaoPay/**").permitAll() // 카카오페이 관련 API 허용

                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/photoAlbums").permitAll()
                        .requestMatchers(HttpMethod.GET, "/boards").permitAll()
                        .requestMatchers(HttpMethod.GET, "/amateurs/ranking").permitAll()
                        .requestMatchers(HttpMethod.GET, "/amateurs/today").permitAll()
                        .requestMatchers(HttpMethod.GET, "/amateurs/ongoing").permitAll()
                        .requestMatchers(HttpMethod.GET, "/amateurs/closing").permitAll()
                        .requestMatchers(HttpMethod.GET, "/amateurs/*").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .anyRequest().authenticated()
                )
                .apply(new JwtSecurityConfig(tokenProvider));
        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JwtFilter 등록

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}

