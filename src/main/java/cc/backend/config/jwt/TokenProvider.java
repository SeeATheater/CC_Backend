package cc.backend.config.jwt;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.ActiveStatus;
import cc.backend.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 5;            //ms단위: 5시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  //ms단위: 7일

    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey, MemberRepository memberRepository,RefreshTokenService refreshTokenService) {

        this.memberRepository = memberRepository;
        this.refreshTokenService = refreshTokenService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDTO generateTokenDto(Member member) {
        // Access Token 생성
        String accessToken = createAccessToken(member);

        // Refresh Token 생성
        String refreshToken = createRefreshToken(member);

        refreshTokenService.saveRefreshToken(member.getId(), refreshToken);

        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private String createAccessToken(Member member) {
        long now = System.currentTimeMillis();

        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(member.getId()))  // payload "sub": "id"
                .claim(AUTHORITIES_KEY, "ROLE_" + member.getRole().name()) // payload "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)    // payload "exp": 151621022 (ex)
                .signWith(key, SignatureAlgorithm.HS512)     // header "alg": "HS512"
                .compact();
    }

    private String createRefreshToken(Member member) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        Long memberId = Long.valueOf(claims.getSubject());

        // Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 활성 상태 확인
        if (member.getActive_status() == ActiveStatus.INACTIVE) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_DEACTIVATED);
        }

         //권한 정보 없이 UserDetails 생성
        UserDetails principal = new CustomUserDetails(member);

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }



    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public TokenDTO refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

        Claims claims = parseClaims(refreshToken);
        Long memberId = Long.valueOf(claims.getSubject());

        if (!refreshTokenService.validateRefreshToken(memberId, refreshToken)) {
            throw new GeneralException(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String newAccessToken = createAccessToken(member);

        return TokenDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) //전에 발급받은 refreshToken은 그대로 유지
                .build();
    }

    public void logout(Long memberId) {
        refreshTokenService.deleteRefreshToken(memberId);
    }
}