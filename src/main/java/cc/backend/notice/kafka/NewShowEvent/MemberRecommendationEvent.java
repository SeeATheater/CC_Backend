package cc.backend.notice.kafka.NewShowEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRecommendationEvent implements Serializable {
    private Long memberId;   // 추천 대상 회원
    private Long noticeId;     // Notice ID
    private String message;  // 개인화 메시지
}
