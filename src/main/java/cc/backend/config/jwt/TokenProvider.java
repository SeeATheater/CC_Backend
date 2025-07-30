package cc.backend.config.jwt;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.ActiveStatus;
import cc.backend.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;


@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 5;            // 5시간
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일

    private final MemberRepository memberRepository;

    private final Key key;

    public TokenProvider(@Value("${jwt.secret}") String secretKey, MemberRepository memberRepository) {
        log.info("JWT Secret Key (Generation): {}", secretKey); // Secret Key 출력

        this.memberRepository = memberRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDTO generateTokenDto(Member member) {
        long now = System.currentTimeMillis();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(member.getEmail())      // payload "sub": "name"
                .claim(AUTHORITIES_KEY, "ROLE_" + member.getRole().name())        // payload "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 151621022 (ex)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(member.getEmail())
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        log.info("🔍 클레임까지 옴 Claims: {}", claims);  // 여기서 claims 값을 확인

        String email = claims.getSubject();

        // Member 조회
        Member member = memberRepository.findMemberByEmail(email)
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
        log.info("JWT Secret Key (Validation): {}", Base64.getEncoder().encodeToString(key.getEncoded())); // Secret Key 출력

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

    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

        Claims claims = parseClaims(refreshToken);
        String email = claims.getSubject();

        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return generateTokenDto(member).getAccessToken();
    }
}