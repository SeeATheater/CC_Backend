package cc.backend.memberLike.service;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.dto.MemberLikeResponseDTO;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberLikeServiceImpl implements MemberLikeService {
    private final MemberLikeRepository memberLikeRepository;
    private final MemberRepository memberRepository;
    private final AmateurShowRepository amateurShowRepository;

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
    public Slice<MemberLikeResponseDTO> getLikedPerformers(Long memberId, Pageable pageable) {
        Member liker = getMemberById(memberId);

        Slice<MemberLike> likeSlice =
                memberLikeRepository.findLikedPerformersSortedBySoonestShow(
                        liker,
                        LocalDateTime.now(),
                        pageable
                );

        List<MemberLikeResponseDTO> content =
                likeSlice.getContent().stream()
                        .map(like -> MemberLikeResponseDTO.builder()
                                .memberLikeId(like.getMemberLikeId())
                                .performerId(like.getPerformer().getId())
                                .performerName(like.getPerformer().getName())
                                .build()
                        )
                        .toList();

        return new SliceImpl<>(content, pageable, likeSlice.hasNext());

    }


    private Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    public boolean hasMemberLikedPerformer(Long likerId, Long performerId) {
        memberRepository.findById(likerId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        memberRepository.findByIdAndRole(performerId, Role.PERFORMER)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_PERFORMER));

        Member liker = getMemberById(likerId);
        Member performer = getMemberById(performerId);
        return memberLikeRepository.existsByLikerAndPerformer(liker, performer);
    }

}
