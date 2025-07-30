package cc.backend.config.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(REFRESH_TOKEN_EXPIRE_TIME));
        log.debug("리프레시 토큰 저장 완료 - 사용자: {}", email);
    }

    // 리프레시 토큰 조회
    public String getRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    // 리프레시 토큰 검증
    public boolean validateRefreshToken(String email, String refreshToken) {
        String savedToken = getRefreshToken(email);
        return savedToken != null && savedToken.equals(refreshToken);
    }

    // 리프레시 토큰 삭제 (로그아웃 시)
    public void deleteRefreshToken(String email) {
        String key = REFRESH_TOKEN_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("리프레시 토큰 삭제 완료 - 사용자: {}", email);
    }
}
