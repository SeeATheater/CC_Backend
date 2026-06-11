package cc.backend.config.jwt;

import cc.backend.member.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;
import java.util.Date;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenProviderLogoutTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private TokenProvider tokenProvider;
    private Key key;

    @BeforeEach
    void setUp() {
        key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String secret = Encoders.BASE64.encode(key.getEncoded());
        tokenProvider = new TokenProvider(secret, memberRepository, refreshTokenService);
    }

    @Test
    void logoutByRefreshTokenDeletesMatchingStoredToken() {
        String refreshToken = refreshToken(11L);
        when(refreshTokenService.validateRefreshToken(11L, refreshToken)).thenReturn(true);

        tokenProvider.logoutByRefreshToken(refreshToken);

        verify(refreshTokenService).deleteRefreshToken(11L);
    }

    @Test
    void logoutByRefreshTokenDoesNotDeleteNonMatchingToken() {
        String refreshToken = refreshToken(11L);
        when(refreshTokenService.validateRefreshToken(11L, refreshToken)).thenReturn(false);

        tokenProvider.logoutByRefreshToken(refreshToken);

        verify(refreshTokenService, never()).deleteRefreshToken(11L);
    }

    @Test
    void logoutByRefreshTokenIgnoresMalformedToken() {
        tokenProvider.logoutByRefreshToken("not-a-jwt");

        verifyNoInteractions(refreshTokenService);
    }

    private String refreshToken(Long memberId) {
        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
