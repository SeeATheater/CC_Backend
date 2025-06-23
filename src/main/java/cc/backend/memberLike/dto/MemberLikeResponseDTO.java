package cc.backend.memberLike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MemberLikeResponseDTO {
    private Long memberLikeId;
    private Long performerId;
    private String performerName;
}
