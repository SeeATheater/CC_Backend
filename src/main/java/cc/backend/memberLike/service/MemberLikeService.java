package cc.backend.memberLike.service;

import cc.backend.memberLike.dto.MemberLikeResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberLikeService {
    MemberLikeResponseDTO likePerformer(Long likerId, Long performerId);
    void cancelLikePerformer(Long likerId, Long performerId);
    Slice<MemberLikeResponseDTO> getLikedPerformers(Long memberId, Pageable pageable);
    boolean hasMemberLikedPerformer(Long memberId, Long performerId);
}
