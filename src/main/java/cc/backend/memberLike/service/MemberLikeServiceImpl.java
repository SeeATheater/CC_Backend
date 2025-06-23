package cc.backend.memberLike.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.dto.MemberLikeResponseDTO;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberLikeServiceImpl implements MemberLikeService {
    private final MemberLikeRepository memberLikeRepository;
    private final MemberRepository memberRepository;

    // 공연진 좋아요
    @Override
    @Transactional
    public MemberLikeResponseDTO likePerformer(Long likerId, Long performerId) {
        Member liker = getMemberById(likerId);
        Member performer = getMemberById(performerId);

        // 좋아요 중복 방지하기
        if (memberLikeRepository.existsByLikerAndPerformer(liker, performer)) {
            throw new GeneralException(ErrorStatus.DUPLICATE_LIKE);
        }

        MemberLike memberLike = MemberLike.builder()
                .liker(liker)
                .performer(performer)
                .build();

        MemberLike saved = memberLikeRepository.save(memberLike);

        return MemberLikeResponseDTO.builder()
                .memberLikeId(saved.getMemberLikeId())
                .performerId(performer.getId())
                .performerName(performer.getName())
                .build();
    }

    // 공연진 좋아요 취소
    @Override
    @Transactional
    public void cancelLikePerformer(Long likerId, Long performerId) {
        Member liker = getMemberById(likerId);
        Member performer = getMemberById(performerId);

        MemberLike like = memberLikeRepository.findByLikerAndPerformer(liker, performer)
                .orElseThrow(() -> new GeneralException(ErrorStatus.LIKE_NOT_FOUND));

        memberLikeRepository.delete(like);
    }

    // 내가 좋아요한 모든 공연진 조회
    @Override
    public List<MemberLikeResponseDTO> getLikedPerformers(Long memberId) {
        Member liker = getMemberById(memberId);

        return liker.getLikesGiven().stream()
                .map(like -> MemberLikeResponseDTO.builder()
                        .memberLikeId(like.getMemberLikeId())
                        .performerId(like.getPerformer().getId())
                        .performerName(like.getPerformer().getName())
                        .build())
                .collect(Collectors.toList());
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

}
