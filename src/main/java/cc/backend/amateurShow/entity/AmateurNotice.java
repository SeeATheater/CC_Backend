package cc.backend.amateurShow.entity;

import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import cc.backend.domain.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmateurNotice extends BaseEntity {

    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String noticeImageUrl;

    private String noticeImageKeyName;

    private String content;

    private String timeInfo;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "amateur_show_id")
    private AmateurShow amateurShow;

    public void update(AmateurUpdateRequestDTO.UpdateNotice dto) {
        if (dto.getContent() != null) this.content = dto.getContent();
        if (dto.getNoticeImageRequestDTO() != null && dto.getNoticeImageRequestDTO().getImageUrl() != null)
            this.noticeImageUrl = dto.getNoticeImageRequestDTO().getImageUrl();
        if (dto.getNoticeImageRequestDTO() != null && dto.getNoticeImageRequestDTO().getKeyName() != null)
            this.noticeImageKeyName = dto.getNoticeImageRequestDTO().getKeyName();
        if (dto.getTimeInfo() != null) this.timeInfo = dto.getTimeInfo();
    }
}
