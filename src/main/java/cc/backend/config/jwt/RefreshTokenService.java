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
    private static final Duration REFRESH_TOKEN_EXPIRE_TIME = Duration.ofDays(7);

    // 리프레시 토큰 저장
    public void saveRefreshToken(Long memberId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);
        log.debug("리프레시 토큰 저장 완료 - 사용자: {}", memberId);
    }

    // 리프레시 토큰 조회
    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        Object token = redisTemplate.opsForValue().get(key);
        return token != null ? token.toString() : null;
    }

    // 리프레시 토큰 검증
    public boolean validateRefreshToken(Long memberId, String refreshToken) {
        String savedToken = getRefreshToken(memberId);
        return savedToken != null && savedToken.equals(refreshToken);
    }

    // 리프레시 토큰 삭제 (로그아웃 시)
    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisTemplate.delete(key);
        log.debug("리프레시 토큰 삭제 완료 - 사용자: {}", memberId);
    }
}
