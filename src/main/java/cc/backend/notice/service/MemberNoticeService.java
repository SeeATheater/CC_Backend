package cc.backend.notice.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.dto.NoticeResponseDTO;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import jakarta.xml.bind.SchemaOutputResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNoticeService {
    private final MemberNoticeRepository memberNoticeRepository;
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    public List<MemberNoticeResponseDTO.MemberNoticeDTO> getAllMemberNotice(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MemberNotice> memberNotices = memberNoticeRepository.findAllByMemberIdAndIsReadOrderByCreatedAtDesc(memberId, false);

        return memberNotices.stream().map(memberNotice -> MemberNoticeResponseDTO.MemberNoticeDTO.builder()
                .id(memberNotice.getId())
                .noticeType(memberNotice.getNotice().getType())
                .message(memberNotice.getNotice().getMessage())
                .isRead(memberNotice.getIsRead())
                .contentId(memberNotice.getNotice().getContentId())
                .createdAt(memberNotice.getCreatedAt())
                .build())
                .toList();
    }

    @Transactional
    public MemberNoticeResponseDTO.MemberNoticeDTO readNotice(Long noticeId, Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        MemberNotice memberNotice = memberNoticeRepository.findById(noticeId)
                .orElseThrow(()->new GeneralException(ErrorStatus.MEMBERNOTICE_NOT_FOUND));

        if(!memberNotice.getMember().getId().equals(memberId)){
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        MemberNotice updatedMemberNotice = memberNotice.updateIsRead();

        return MemberNoticeResponseDTO.MemberNoticeDTO.builder()
                .id(updatedMemberNotice.getId())
                .noticeType(updatedMemberNotice.getNotice().getType())
                .message(updatedMemberNotice.getNotice().getMessage())
                .isRead(true)
                .contentId(updatedMemberNotice.getNotice().getContentId())
                .createdAt(memberNotice.getCreatedAt())
                .build();
    }

}
