package cc.backend.memberLike.service;

import cc.backend.memberLike.dto.MemberLikeResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberLikeService {
    MemberLikeResponseDTO likePerformer(Long likerId, Long performerId);
    void cancelLikePerformer(Long likerId, Long performerId);
    List<MemberLikeResponseDTO> getLikedPerformers(Long memberId);
    boolean hasMemberLikedPerformer(Long memberId, Long performerId);
}
