package cc.backend.notice.kafka.NewShowEvent;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.repository.AmateurShowRepository;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.memberLike.entity.MemberLike;
import cc.backend.memberLike.repository.MemberLikeRepository;
import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import cc.backend.notice.event.entity.NewShowEvent;

import cc.backend.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberRecommendationProducer {

    private final MemberLikeRepository memberLikeRepository;
    private final AmateurShowRepository amateurShowRepository;
    private final NoticeRepository noticeRepository;
    private final KafkaTemplate<String, MemberRecommendationEvent> kafkaTemplate;

    private static final String TOPIC = "member-recommendation-event";
    private static final int BATCH_SIZE = 50;


    public void recommendByHashtag(NewShowEvent event) {
        if (event == null) return;

        Long showId = event.getAmateurShowId();
        Long performerId = event.getPerformerId();

        Notice notice = createNoticeForNewShow(showId);

        // 모든 회원 조회
        List<Member> allMembers = memberLikeRepository.findAllDistinctMembers();

        // 추천 대상 회원 필터링 및 Kafka 이벤트 발행
        sendRecommendations(allMembers, notice, showId);

    }

    //DB 트랜잭션 안에서 Notice 생성
    @Transactional
    protected Notice createNoticeForNewShow(Long showId) {
        AmateurShow newShow = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        Set<String> newTagsSet = Arrays.stream(Optional.ofNullable(newShow.getHashtag()).orElse("").split("[#,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (newTagsSet.isEmpty()) {
            throw new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND); // 태그 없으면 추천 불가
        }

        Notice notice = noticeRepository.save(
                Notice.builder()
                        .type(NoticeType.RECOMMEND)
                        .message("새로운 공연 '" + newShow.getName() + "' 어떠세요? ")
                        .contentId(newShow.getId())
                        .build()
        );

        return notice;
    }

    protected  void sendRecommendations(List<Member> members, Notice notice, Long showId) {
        AmateurShow newShow = amateurShowRepository.findById(showId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AMATEURSHOW_NOT_FOUND));

        Set<String> newTagsSet = Arrays.stream(Optional.ofNullable(newShow.getHashtag()).orElse("").split("[#,\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        List<MemberRecommendationEvent> batchEvents = new ArrayList<>();

        for (Member member : members) {
            boolean shouldRecommend = memberShouldBeRecommended(member, newTagsSet);

            if (shouldRecommend) {
                // 회원별 msg 생성
                String personalMsg = "새로운 공연 '" + newShow.getName() + "' 어떠세요? "
                        + newShow.getHashtag() + " " + member.getName() + "님 취향에 딱!";

                batchEvents.add(new MemberRecommendationEvent(member.getId(), notice.getId(), personalMsg));

                // 배치 단위로 Kafka 이벤트 발행 (DB 처리는 Consumer에서)
                if (batchEvents.size() >= BATCH_SIZE) {
                    sendBatchToKafka(batchEvents);
                    batchEvents.clear();
                }
            }
        }
        if (!batchEvents.isEmpty()) {
            sendBatchToKafka(batchEvents);
        }
    }


    //Kafka 이벤트 비동기 전송 (Transactional 밖)
    private void sendBatchToKafka(List<MemberRecommendationEvent> events) {
        for (MemberRecommendationEvent event : events) {
            kafkaTemplate.send(TOPIC, event.getMemberId().toString(), event);
            System.out.println("Kafka event sent to topic " + TOPIC + ": " + event);
        }

    }

    //회원 추천 여부 판단 (좋아요 유무 관계없이 해시태그 겹치면 true)
    private boolean memberShouldBeRecommended(Member member, Set<String> newTagsSet) {
        // 회원이 좋아요한 공연자 목록 조회
        List<MemberLike> likedPerformers = memberLikeRepository.findByLikerId(member.getId());

        //아무 계정도 좋아요하지 않은 멤버에게는 추천 X
        if (likedPerformers.isEmpty()) {
           return false;
        }

        for (MemberLike like : likedPerformers) {
            Long likedPerformerId = like.getPerformer().getId();

            // 좋아요한 공연자의 기존 공연 해시태그 조회
            List<String> hashtags = amateurShowRepository.findHashtagsByMemberId(likedPerformerId);

            for (String existingHashtags : hashtags) {
                Set<String> existingTagsSet = Arrays.stream(existingHashtags.split("#"))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                Set<String> intersection = new HashSet<>(newTagsSet);
                intersection.retainAll(existingTagsSet);

                if (!intersection.isEmpty()) {
                    return true;    // 공통 해시태그 존재시 추천
                }
            }
        }

        return false;
    }
}
