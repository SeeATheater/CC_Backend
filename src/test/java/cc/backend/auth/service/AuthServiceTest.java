package cc.backend.auth.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.auth.kakao.KakaoClient;
import cc.backend.auth.kakao.dto.response.KakaoTokenResponse;
import cc.backend.auth.kakao.dto.response.KakaoUserInfo;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String REDIRECT_URI = "https://seeatheater.store/auth/kakao/callback";

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TokenProvider tokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(kakaoClient, memberRepository, tokenProvider);
    }

    @Test
    void kakaoLoginCreatesMemberWhenOptionalNameAndPhoneAreMissing() throws Exception {
        KakaoTokenResponse kakaoToken = kakaoToken();
        KakaoUserInfo userInfo = kakaoUserInfo("""
                {
                  "id": 12345,
                  "kakao_account": {
                    "email": "seeatheater@gmail.com"
                  },
                  "properties": {
                    "nickname": "SeeATheater"
                  }
                }
                """);
        TokenDTO tokenDTO = TokenDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(kakaoClient.getAccessToken("code", REDIRECT_URI)).thenReturn(kakaoToken);
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(memberRepository.findMemberByKakaoId("12345")).thenReturn(Optional.empty());
        when(memberRepository.findMemberByEmail("seeatheater@gmail.com")).thenReturn(Optional.empty());
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateTokenDto(any(Member.class))).thenReturn(tokenDTO);

        TokenDTO result = authService.kakaoLogin("code", REDIRECT_URI, Role.AUDIENCE);

        assertThat(result).isSameAs(tokenDTO);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getEmail()).isEqualTo("seeatheater@gmail.com");
        assertThat(savedMember.getRole()).isEqualTo(Role.AUDIENCE);
        assertThat(savedMember.getKakaoId()).isEqualTo("12345");
        assertThat(savedMember.getName()).isEqualTo("SeeATheater");
        assertThat(savedMember.getUsername()).startsWith("SeeATheater");
        assertThat(savedMember.getPhone()).isNull();
    }

    @Test
    void kakaoLoginFallsBackToEmailLocalPartWhenNicknameNameAndPhoneAreMissing() throws Exception {
        KakaoTokenResponse kakaoToken = kakaoToken();
        KakaoUserInfo userInfo = kakaoUserInfo("""
                {
                  "id": 67890,
                  "kakao_account": {
                    "email": "seeatheater@gmail.com"
                  }
                }
                """);
        TokenDTO tokenDTO = TokenDTO.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(kakaoClient.getAccessToken("code", REDIRECT_URI)).thenReturn(kakaoToken);
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(memberRepository.findMemberByKakaoId("67890")).thenReturn(Optional.empty());
        when(memberRepository.findMemberByEmail("seeatheater@gmail.com")).thenReturn(Optional.empty());
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateTokenDto(any(Member.class))).thenReturn(tokenDTO);

        authService.kakaoLogin("code", REDIRECT_URI, Role.AUDIENCE);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member savedMember = memberCaptor.getValue();
        assertThat(savedMember.getName()).isEqualTo("seeatheater");
        assertThat(savedMember.getUsername()).startsWith("seeatheater");
        assertThat(savedMember.getPhone()).isNull();
    }

    @Test
    void kakaoLoginRejectsUserInfoWithoutEmail() throws Exception {
        KakaoTokenResponse kakaoToken = kakaoToken();
        KakaoUserInfo userInfo = kakaoUserInfo("""
                {
                  "id": 12345,
                  "kakao_account": {},
                  "properties": {
                    "nickname": "SeeATheater"
                  }
                }
                """);

        when(kakaoClient.getAccessToken("code", REDIRECT_URI)).thenReturn(kakaoToken);
        when(kakaoClient.getUserInfo("kakao-access-token")).thenReturn(userInfo);

        assertThatThrownBy(() -> authService.kakaoLogin("code", REDIRECT_URI, Role.AUDIENCE))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getCode()).isEqualTo(ErrorStatus.INVALID_KAKAO_USER_INFO));
    }

    private KakaoTokenResponse kakaoToken() throws Exception {
        return objectMapper.readValue("""
                {
                  "access_token": "kakao-access-token",
                  "refresh_token": "kakao-refresh-token"
                }
                """, KakaoTokenResponse.class);
    }

    private KakaoUserInfo kakaoUserInfo(String json) throws Exception {
        return objectMapper.readValue(json, KakaoUserInfo.class);
    }
}
