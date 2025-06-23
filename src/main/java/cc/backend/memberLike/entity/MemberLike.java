package cc.backend.memberLike.entity;

import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberLikeId;

    @ManyToOne
    @JoinColumn(name = "liker_id", nullable = false)
    private Member liker;

    @ManyToOne
    @JoinColumn(name = "performer_id", nullable = false)
    private Member performer;

    private String performerName;
}

