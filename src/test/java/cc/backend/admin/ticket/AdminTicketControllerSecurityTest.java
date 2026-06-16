package cc.backend.admin.ticket;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.backend.apiPayLoad.PageResponse;
import cc.backend.config.jwt.JwtFilter;
import cc.backend.config.jwt.MethodSecurityConfig;
import cc.backend.config.jwt.SecurityConfig;
import cc.backend.config.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminTicketController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, AdminTicketControllerSecurityTest.TestSecurityBeans.class})
@TestPropertySource(properties = "cors.allowed-origins=http://localhost:*")
class AdminTicketControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTicketService adminTicketService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessTicketHistory() throws Exception {
        when(adminTicketService.getTicketList(anyInt(), anyInt(), nullable(String.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/admin/ticket/history?page=0&size=20"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "AUDIENCE")
    void audienceCannotAccessTicketHistory() throws Exception {
        mockMvc.perform(get("/admin/ticket/history?page=0&size=20"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminTicketService);
    }

    @Test
    @WithMockUser(roles = "PERFORMER")
    void performerCannotAccessTicketHistory() throws Exception {
        mockMvc.perform(get("/admin/ticket/history?page=0&size=20"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminTicketService);
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
