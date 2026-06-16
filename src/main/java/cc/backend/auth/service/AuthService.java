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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    public TokenDTO kakaoLogin(String authorizationCode, String redirectUri,Role role) {
        // 인가 코드로 액세스 토큰 획득
        KakaoTokenResponse tokenResponse = kakaoClient.getAccessToken(authorizationCode, redirectUri);

        KakaoUserInfo userInfo = kakaoClient.getUserInfo(tokenResponse.getAccessToken());

        // 기존 회원 조회 또는 신규 회원 생성
        Member member = findOrCreateMember(userInfo, role);

        return tokenProvider.generateTokenDto(member);
    }

    private Member findOrCreateMember(KakaoUserInfo userInfo, Role role) {
        if (userInfo == null || userInfo.getId() == null || userInfo.getKakaoAccount() == null) {
            throw new GeneralException(ErrorStatus.INVALID_KAKAO_USER_INFO);
        }

        String email = userInfo.getKakaoAccount().getEmail();
        if (!StringUtils.hasText(email)) {
            throw new GeneralException(ErrorStatus.INVALID_KAKAO_USER_INFO);
        }

        String kakaoId = userInfo.getId().toString();

        // 1차: kakaoId로 조회
        Optional<Member> existingMember = memberRepository.findMemberByKakaoId(kakaoId);

        // 2차: kakaoId에 해당하는 계정 없는 경우, 이메일 대조로 기존 회원 조회
        if (existingMember.isEmpty() && email != null) {
            log.info("kakaoId로 회원 미발견, email로 2차 조회: {}", email);
            Optional<Member> emailMember = memberRepository.findMemberByEmail(email);

            if (emailMember.isPresent()) {
                Member member = emailMember.get();

                //같은 이메일이 등록된 서로 다른 kakao계정 존재(A,B)
                // A계정으로 서비스 가입,
                // B계정으로 로그인 시도할 경우, B의 kakaoId가 없어 이메일로 로그인 시도
                // 해당 이메일로 멤버 조회시 기존 계정인 A가 반환
                // A와 연결된 멤버의 kakaoId가 B로 바뀌는 email fallback으로 우회 로그인 문제 발생 가능
                // 우회 로그인 에러 던짐
                if (member.getKakaoId() != null && !member.getKakaoId().equals(kakaoId)) {
                    log.warn("Email fallback blocked. email={}, existingKakaoId={}, loginKakaoId={}",
                            email, member.getKakaoId(), kakaoId);
                    throw new GeneralException(ErrorStatus.INVALID_KAKAO_USER_INFO);
                }
                existingMember = emailMember;   // 🔥 이거 필요

            }
        }

        //기존 멤버인 경우 먼저 반환
        if (existingMember.isPresent()) {
            Member member = existingMember.get();

            //활성 회원인 경우
            if(member.getActive_status()==ActiveStatus.ACTIVE){
                //다른 역할로 로그인 요청한 경우
                if (!member.getRole().equals(role)) {
                    throw new GeneralException(ErrorStatus.MEMBER_ROLE_ALREADY_EXISTS);
                }

                //기존 계정이 이메일로만 가입한 경우 kakaoId 자동 연동
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

        // 기존 멤버가 아닌 경우 새 맴버 생성
        String nickname = resolveNickname(userInfo, email);
        String name = resolveName(userInfo, nickname);
        String encryptedPhone = encryptPhoneIfPresent(userInfo.getKakaoAccount().getPhoneNumber());

        return createNewMember(email, nickname, name, role, kakaoId, encryptedPhone);
    }

    private Member createNewMember(String email, String nickname, String name, Role role, String kakaoId, String encryptedPhone) {
        String username = generateUsername(nickname);

        Member newMember = Member.builder()
                .username(username)
                .name(name)
                .email(email)
                .phone(encryptedPhone)
                .role(role)
                .kakaoId(kakaoId)
                .build();

        //멱등성 문제 방지(duplicate-write handling)
        try {
            return memberRepository.save(newMember);

        } catch (DataIntegrityViolationException e) {
            //동시에 들어온 두번째 요청이 save실패 후 catch 구문으로 빠져 첫 요청으로 생성된 멤버를 반환하도록
            log.warn("Concurrent kakao signup detected. retry lookup. kakaoId={}", kakaoId);

            Optional<Member> kakaoMember = memberRepository.findMemberByKakaoId(kakaoId);
            if (kakaoMember.isPresent()) {
                return kakaoMember.get();
            }

            if (email != null) {
                Optional<Member> emailMember = memberRepository.findMemberByEmail(email);
                if (emailMember.isPresent()) {
                    Member member = emailMember.get();
                    // email fallback은 kakaoId가 없는 계정에만 허용
                    // 이메일 가입 계정만 허용
                    if (member.getKakaoId() == null) {
                        member.updateKakaoId(kakaoId);
                        return member;
                    }
                }
            }

            throw e;
        }
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
        String email = userInfo.getKakaoAccount().getEmail();
        String nickname = resolveNickname(userInfo, email);
        String name = resolveName(userInfo, nickname);
        String newUsername = generateUsername(nickname);

        String encryptedPhone = encryptPhoneIfPresent(userInfo.getKakaoAccount().getPhoneNumber());

        // 회원 정보 업데이트 및 재활성화
        member.reactivateMember();
        member.updateRole(newRole); // 역할 업데이트
        member.updateName(name);
        member.updateUsername(newUsername);
        member.updatePhone(encryptedPhone);

        // CASE 1: 기존 계정이 이메일로만 가입되어 kakaoId가 없는 경우
        // → 현재 로그인한 kakaoId를 연결하여 계정을 재활성화
        if (member.getKakaoId() == null) {
            member.updateKakaoId(kakaoId);
        } else if (!member.getKakaoId().equals(kakaoId)) {
            // CASE 2: 기존 계정에 이미 kakaoId가 연결되어 있지만,
            // 로그인 시도한 kakaoId와 다른 경우
            // → email fallback을 통한 계정 탈취 가능성이 있으므로 로그인 차단
            log.warn("Reactivation blocked. memberId={}, existingKakaoId={}, loginKakaoId={}",
                    member.getId(), member.getKakaoId(), kakaoId);
            throw new GeneralException(ErrorStatus.INVALID_KAKAO_USER_INFO);
        }

        // CASE 3: 기존 계정의 kakaoId와 현재 로그인한 kakaoId가 동일한 경우
        // → 정상적인 재로그인이므로 추가 처리 없이 그대로 진행
        return member;
    }

    private String resolveNickname(KakaoUserInfo userInfo, String email) {
        if (userInfo.getProperties() != null && StringUtils.hasText(userInfo.getProperties().getNickname())) {
            return userInfo.getProperties().getNickname();
        }

        int atIndex = email.indexOf("@");
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String resolveName(KakaoUserInfo userInfo, String fallbackName) {
        if (userInfo.getKakaoAccount() != null && StringUtils.hasText(userInfo.getKakaoAccount().getName())) {
            return userInfo.getKakaoAccount().getName();
        }

        return fallbackName;
    }

    private String encryptPhoneIfPresent(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }

        try {
            return AESUtil.encrypt(phoneNumber);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.PHONENUM_ENCRYPT_FAIL);
        }
    }
}
