package cc.backend.auth.review;

import cc.backend.member.enumerate.Role;

import java.util.Map;

/**
 * [임시] 앱 심사용 일반 로그인 허용 계정 화이트리스트.
 *
 * 비밀번호는 DB에 직접 INSERT 한 계정의 값으로 검증한다.
 * 심사 통과 후 review 패키지 전체와 SecurityConfig 변경분을 함께 revert 한다.
 */
public final class ReviewAccounts {

    private ReviewAccounts() {
    }

    /** 일반 로그인을 허용할 심사용 계정 (이메일 -> 역할) */
    public static final Map<String, Role> WHITELIST = Map.of(
            "user1@test.com", Role.AUDIENCE,
            "performer1@test.com", Role.PERFORMER
    );
}
