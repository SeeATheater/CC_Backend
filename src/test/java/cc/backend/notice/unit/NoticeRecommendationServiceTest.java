package cc.backend.notice.unit;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.enums.ApprovalStatus;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.amateurShow.repository.projection.PerformerHashtagView;
import cc.backend.board.repository.BoardRepository;
import cc.backend.board.repository.CommentRepository;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import cc.backend.memberLike.repository.MemberLikeRepository;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.MemberNotice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.repository.MemberNoticeRepository;
import cc.backend.notice.repository.NoticeRepository;
import cc.backend.notice.service.NoticeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 추천 대상 조회가 전체 MemberLike를 읽지 않고 태그가 일치하는 공연자 ID로
 * 좋아요 조회 범위를 먼저 제한하는지 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class NoticeRecommendationServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberNoticeRepository memberNoticeRepository;
    @Mock private AmateurShowRepository amateurShowRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private MemberLikeRepository memberLikeRepository;
    @Mock private AmateurShow newShow;
    @Mock private Member targetMember;
    @Mock private PerformerHashtagView matchingPerformer;
    @Mock private PerformerHashtagView unrelatedPerformer;

    @InjectMocks
    private NoticeServiceImpl noticeService;

    @Test
    void narrowsMemberLikeQueryToPerformersWithOverlappingApprovedShowTags() {
        ApprovalShowEvent event = ApprovalShowEvent.create(100L, 200L);

        when(noticeRepository.existsByContentIdAndType(100L, NoticeType.RECOMMEND))
                .thenReturn(false);
        when(amateurShowRepository.findById(100L)).thenReturn(Optional.of(newShow));
        when(newShow.getId()).thenReturn(100L);
        when(newShow.getName()).thenReturn("신규 연극");
        when(newShow.getHashtag()).thenReturn("#연극 #고전");

        when(matchingPerformer.getPerformerId()).thenReturn(10L);
        when(matchingPerformer.getHashtag()).thenReturn("#연극 #코미디");
        when(unrelatedPerformer.getHashtag()).thenReturn("#재즈 #콘서트");
        when(amateurShowRepository.findApprovedHistoricalPerformerHashtags(
                ApprovalStatus.APPROVED,
                100L
        )).thenReturn(List.of(matchingPerformer, unrelatedPerformer));

        when(memberLikeRepository.findDistinctLikersByPerformerIdIn(Set.of(10L)))
                .thenReturn(List.of(targetMember));
        when(targetMember.getName()).thenReturn("테스트회원");
        when(noticeRepository.save(any(Notice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        noticeService.notifyRecommendation(event);

        verify(memberLikeRepository).findDistinctLikersByPerformerIdIn(Set.of(10L));
        verify(memberLikeRepository, never())
                .findDistinctLikersByPerformerIdIn(Set.of(20L));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<MemberNotice>> noticesCaptor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(memberNoticeRepository).saveAll(noticesCaptor.capture());

        List<MemberNotice> savedNotices = StreamSupport
                .stream(noticesCaptor.getValue().spliterator(), false)
                .toList();
        assertThat(savedNotices).hasSize(1);
        assertThat(savedNotices.get(0).getMember()).isSameAs(targetMember);
        assertThat(savedNotices.get(0).resolveMessage()).contains("테스트회원");
    }
}
