package cc.backend.admin.amateurShow.dto;


import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAmateurShowSummaryResponseDTO {
    private Long showId;
    private String showName;
    private String performerName;
    private String performerEmail;
    private LocalDateTime createdAt;
    private String hashTag;
    private String summary;
    private String account;
    private String contact;
    private AmateurShowStatus showStatus;

    public static AdminAmateurShowSummaryResponseDTO from(AmateurShow s) {
        Member m = s.getMember();
        return AdminAmateurShowSummaryResponseDTO.builder()
                .showId(s.getId())
                .showName(s.getName())
                .performerName(m != null ? m.getName() : null)
                .performerEmail(m != null ? m.getEmail() : null)
                .createdAt(s.getCreatedAt())
                .hashTag(s.getHashtag())
                .summary(s.getSummary())
                .account(s.getAccount())
                .contact(s.getContact())
                .showStatus(s.getStatus())
                .build();
    }

}
