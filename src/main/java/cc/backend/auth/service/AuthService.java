package cc.backend.auth.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.auth.kakao.dto.response.KakaoTokenResponse;
import cc.backend.auth.kakao.dto.response.KakaoUserInfo;
import cc.backend.auth.kakao.KakaoClient;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenDTO kakaoLogin(String authorizationCode, Role role) {
        // 인가 코드로 액세스 토큰 획득
        KakaoTokenResponse tokenResponse = kakaoClient.getAccessToken(authorizationCode);

        KakaoUserInfo userInfo = kakaoClient.getUserInfo(tokenResponse.getAccessToken());

        // 기존 회원 조회 또는 신규 회원 생성
        Member member = findOrCreateMember(userInfo, role);

        return tokenProvider.generateTokenDto(member);
    }

    private Member findOrCreateMember(KakaoUserInfo userInfo, Role role) {
        String email = userInfo.getKakaoAccount().getEmail();
        String nickname = userInfo.getProperties().getNickname();
        String kakaoId = userInfo.getId().toString();

        if (email == null || nickname == null) {
            throw new GeneralException(ErrorStatus.INVALID_KAKAO_USER_INFO);
        }

        Optional<Member> existingMember = memberRepository.findMemberByEmail(email);

        if (existingMember.isPresent()) {
            Member member = existingMember.get();

            //다른 역할로 로그인 요청한 경우
            if (!member.getRole().equals(role)) {
                throw new GeneralException(ErrorStatus.MEMBER_ROLE_ALREADY_EXISTS);
            }

            //같은 역할일 경우 로그인 처리
            if (member.getKakaoId() == null) {
                member.updateKakaoId(kakaoId);
            }
            member.updateNickname(nickname);
            return member;
        }

        return createNewMember(userInfo, role, kakaoId);
    }

    private Member createNewMember(KakaoUserInfo userInfo, Role role, String kakaoId) {
        String email = userInfo.getKakaoAccount().getEmail();
        String nickname = userInfo.getProperties().getNickname();

        String username = generateUsername(email, role);

        Member newMember = Member.builder()
                .username(username)
                .name(nickname)
                .email(email)
                .role(role)
                .kakaoId(kakaoId)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build();

        return memberRepository.save(newMember);
    }

    //유저네임 = 이메일 + 역할
    private String generateUsername(String email, Role role) {
        String baseUsername = email.split("@")[0] + "_" + role.name().toLowerCase();

        String username = baseUsername;
        int counter = 1;
        while (memberRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter;
            counter++;
        }

        return username;
    }
}
