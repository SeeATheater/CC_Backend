package cc.backend.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SecurityConfigPublicEndpointBaselineTest.BaselineController.class)
@Import({SecurityConfig.class, SecurityConfigPublicEndpointBaselineTest.TestSecurityBeans.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:*")
class SecurityConfigPublicEndpointBaselineTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithAnonymousUser
    void publicEndpointsAreNotBlockedByAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(notAuthBlocked());
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(notAuthBlocked());
        mockMvc.perform(get("/kakaoPay/approve"))
                .andExpect(notAuthBlocked());
        mockMvc.perform(get("/kakaoPay/cancel"))
                .andExpect(notAuthBlocked());
        mockMvc.perform(get("/kakaoPay/fail"))
                .andExpect(notAuthBlocked());
    }

    @Test
    @WithAnonymousUser
    void privateEndpointsBlockAnonymousRequests() throws Exception {
        mockMvc.perform(post("/kakaoPay/ready?tempTicketId=1"))
                .andExpect(authBlocked());
        mockMvc.perform(get("/myTickets/list"))
                .andExpect(authBlocked());
        mockMvc.perform(get("/member/myPage"))
                .andExpect(authBlocked());
        mockMvc.perform(get("/admin/dashboard/approval"))
                .andExpect(authBlocked());
    }

    private static ResultMatcher notAuthBlocked() {
        return result -> assertThat(result.getResponse().getStatus())
                .isNotIn(HttpServletResponse.SC_UNAUTHORIZED, HttpServletResponse.SC_FORBIDDEN);
    }

    private static ResultMatcher authBlocked() {
        return result -> assertThat(result.getResponse().getStatus())
                .isIn(HttpServletResponse.SC_UNAUTHORIZED, HttpServletResponse.SC_FORBIDDEN);
    }

    @RestController
    static class BaselineController {

        @GetMapping({
                "/actuator/health",
                "/v3/api-docs",
                "/kakaoPay/approve",
                "/kakaoPay/cancel",
                "/kakaoPay/fail"
        })
        String publicEndpoint() {
            return "ok";
        }

        @PostMapping("/kakaoPay/ready")
        String kakaoPayReady() {
            return "ok";
        }

        @GetMapping({
                "/myTickets/list",
                "/member/myPage",
                "/admin/dashboard/approval"
        })
        String privateEndpoint() {
            return "ok";
        }
    }

    @TestConfiguration
    static class TestSecurityBeans {

        @Bean
        TokenProvider tokenProvider() {
            return mock(TokenProvider.class);
        }

        @Bean
        JwtFilter jwtFilter(TokenProvider tokenProvider) {
            return new PassThroughJwtFilter(tokenProvider);
        }
    }

    private static class PassThroughJwtFilter extends JwtFilter {

        PassThroughJwtFilter(TokenProvider tokenProvider) {
            super(tokenProvider);
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            if (HttpMethod.OPTIONS.matches(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            filterChain.doFilter(request, response);
        }
    }
}
