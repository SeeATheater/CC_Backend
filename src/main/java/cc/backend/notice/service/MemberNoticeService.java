package cc.backend.notice.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.notice.dto.MemberNoticeResponseDTO;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    public MemberNoticeResponseDTO.MemberNoticeListDTO getAllMemberNotice(
            Long memberId, Long cursorId, LocalDateTime cursorCreatedAt, int pageSize) {

        memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MemberNotice> memberNotices =
                memberNoticeRepository.findMemberNoticeByCursor(
                        memberId,
                        cursorId,
                        cursorCreatedAt,
                        PageRequest.of(0, pageSize + 1));

        boolean hasNext = memberNotices.size() > pageSize;

        List<MemberNotice> limited = memberNotices.stream()
                .limit(pageSize)
                .toList();

        List<MemberNoticeResponseDTO.MemberNoticeDTO> items = limited.stream()
                .map(notice -> MemberNoticeResponseDTO.MemberNoticeDTO.builder()
                        .id(notice.getId())
                        .noticeType(notice.getNotice().getType())
                        .message(notice.getNotice().getMessage())
                        .isRead(notice.isRead())
                        .contentId(notice.getNotice().getContentId())
                        .createdAt(notice.getCreatedAt())
                        .build())
                .toList();


        Long nextCursorId = hasNext ? limited.get(limited.size() - 1).getId() : null;
        LocalDateTime nextCursorCreatedAt = hasNext ? limited.get(limited.size() - 1).getCreatedAt() : null;


        return MemberNoticeResponseDTO.MemberNoticeListDTO.builder()
                .items(items)
                .meta(MemberNoticeResponseDTO.MemberNoticeListDTO.Meta.builder()
                        .count(items.size())
                        .hasNext(hasNext)
                        .empty(items.isEmpty())
                        .nextCursorId(nextCursorId)
                        .nextCursorCreatedAt(nextCursorCreatedAt)
                        .build())
                .build();
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

        memberNotice.updateIsRead();

        return MemberNoticeResponseDTO.MemberNoticeDTO.builder()
                .id(memberNotice.getId())
                .noticeType(memberNotice.getNotice().getType())
                .message(memberNotice.getNotice().getMessage())
                .isRead(memberNotice.isRead())
                .contentId(memberNotice.getNotice().getContentId())
                .createdAt(memberNotice.getCreatedAt())
                .build();
    }
}
