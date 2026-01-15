package cc.backend.config.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final JwtFilter jwtFilter;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);

        // 배포 환경
        config.addAllowedOriginPattern("https://seeatheater.site");
        config.addAllowedOriginPattern("https://www.seeatheater.site");
        config.addAllowedOriginPattern("https://api.seeatheater.site");

        // 개발 환경
        config.addAllowedOriginPattern("http://localhost:*");

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // HTTP Basic 비활성화
                .formLogin(formLogin -> formLogin.disable()) // 폼 로그인 비활성화
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()  // 헬스체크 허용
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/kakaoPay/**").permitAll() // 카카오페이 관련 API 허용

                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()
                        .requestMatchers("/admin/login").permitAll()

                        .requestMatchers(HttpMethod.GET, "/photoAlbums").permitAll()
                        .requestMatchers(HttpMethod.GET, "/boards").permitAll()
                        .requestMatchers(HttpMethod.GET, "/boards/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/boards/hot").permitAll()
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

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
            ROLE_ADMIN > ROLE_PERFORMER
            ROLE_PERFORMER > ROLE_AUDIENCE
        """);
    }
}

