package cc.backend.auth.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.auth.kakao.dto.response.KakaoTokenResponse;
import cc.backend.auth.kakao.dto.response.KakaoUserInfo;
import cc.backend.auth.kakao.KakaoClient;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.ActiveStatus;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import cc.backend.member.util.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final KakaoClient kakaoClient;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final Random random = new Random();

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
        String name = userInfo.getKakaoAccount().getName();
        String phoneNumber = userInfo.getKakaoAccount().getPhoneNumber();

        String kakaoId = userInfo.getId().toString();

       if (email == null || nickname == null || name == null || phoneNumber == null)  {
            log.error("필수 정보 누락 - 이메일: {}, 닉네임: {}, 이름: {}, 전화번호: {}",
                    email, nickname, name, phoneNumber);
            throw new GeneralException(ErrorStatus.INVALID_KAKAO_USER_INFO);
        }

        Optional<Member> existingMember = memberRepository.findMemberByEmail(email);

        if (existingMember.isPresent()) {
            Member member = existingMember.get();

            //활성 회원인 경우
            if(member.getActive_status()==ActiveStatus.ACTIVE){
                //다른 역할로 로그인 요청한 경우
                if (!member.getRole().equals(role)) {
                    throw new GeneralException(ErrorStatus.MEMBER_ROLE_ALREADY_EXISTS);
                }

                //같은 역할일 경우 로그인 처리
                if (member.getKakaoId() == null) {
                    member.updateKakaoId(kakaoId);
                }

                return member;
            }

            // 비활성화된 회원인 경우 - 재가입 처리
            if (member.getActive_status() == ActiveStatus.INACTIVE) {
                return reactivateMember(member, userInfo, role, kakaoId);
            }

        }

        return createNewMember(userInfo, role, kakaoId);
    }

    private Member createNewMember(KakaoUserInfo userInfo, Role role, String kakaoId) {
        String email = userInfo.getKakaoAccount().getEmail();
        String nickname = userInfo.getProperties().getNickname();
        String name = userInfo.getKakaoAccount().getName();
        String encryptedPhone = "";
        try {
            encryptedPhone = AESUtil.encrypt(userInfo.getKakaoAccount().getPhoneNumber());
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.PHONENUM_ENCRYPT_FAIL);
        }

        String username = generateUsername(nickname);

        Member newMember = Member.builder()
                .username(username)
                .name(name)
                .email(email)
                .phone(encryptedPhone)
                .role(role)
                .kakaoId(kakaoId)
                .build();

        return memberRepository.save(newMember);
    }

    //유저네임 = 닉네임 + 랜덤숫자 3개
    private String generateUsername(String nickname) {
        String username;
        int maxAttempts = 100; // 무한 루프 방지를 위한 최대 시도 횟수
        int attempts = 0;

        do {
            // 3자리 랜덤 숫자 생성 (000 ~ 999)
            String randomSuffix = String.format("%03d", random.nextInt(1000));
            username = nickname + randomSuffix;
            attempts++;

            if (attempts >= maxAttempts) {
                // 최대 시도 횟수 초과 시 타임스탬프 추가
                username = nickname + System.currentTimeMillis() % 1000;
                break;
            }

        } while (memberRepository.existsByUsername(username));

        return username;
    }

    //비활성화된 회원 재활성화
    private Member reactivateMember(Member member, KakaoUserInfo userInfo, Role newRole, String kakaoId) {
        String name = userInfo.getKakaoAccount().getName();
        String phoneNumber = userInfo.getKakaoAccount().getPhoneNumber();
        String nickname = userInfo.getProperties().getNickname();

        String newUsername = generateUsername(nickname);

        // 회원 정보 업데이트 및 재활성화
        member.reactivateMember();
        member.updateRole(newRole); // 역할 업데이트
        member.updateName(name);
        member.updateUsername(newUsername);
        member.updatePhone(phoneNumber);

        if (member.getKakaoId() == null) {
            member.updateKakaoId(kakaoId);
        }


        return member;
    }
}
