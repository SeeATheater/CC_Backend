package cc.backend.notice.event.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewShowEvent {
    private Long amateurShowId;   // 공연 ID
    private Long performerId;     // 공연 등록자 ID
    private List<Long> likerIds;  // 좋아요한 유저 ID 리스트
}
