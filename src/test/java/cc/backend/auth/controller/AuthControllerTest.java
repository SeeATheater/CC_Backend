package cc.backend.auth.controller;

import cc.backend.config.jwt.CustomUserDetails;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.RefreshTokenRequest;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(tokenProvider);
    }

    @Test
    void logoutDeletesRefreshTokenForAuthenticatedMember() {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(7L);
        when(authentication.getPrincipal()).thenReturn(new CustomUserDetails(member));

        assertThat(authController.logout(authentication, null).getStatusCode().is2xxSuccessful()).isTrue();

        verify(tokenProvider).logout(7L);
        verify(tokenProvider, never()).logoutByRefreshToken(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void logoutUsesRefreshTokenWhenAccessTokenAuthenticationIsMissing() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        assertThat(authController.logout(null, request).getStatusCode().is2xxSuccessful()).isTrue();

        verify(tokenProvider).logoutByRefreshToken("refresh-token");
    }

    @Test
    void logoutWithoutAuthenticationOrBodyIsIdempotent() {
        assertThat(authController.logout(null, null).getStatusCode().is2xxSuccessful()).isTrue();

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void refreshDelegatesToTokenProvider() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        TokenDTO tokenDTO = TokenDTO.builder()
                .accessToken("new-access-token")
                .refreshToken("refresh-token")
                .build();
        when(tokenProvider.refreshAccessToken("refresh-token")).thenReturn(tokenDTO);

        assertThat(authController.refresh(request).getBody()).isSameAs(tokenDTO);
    }
}
