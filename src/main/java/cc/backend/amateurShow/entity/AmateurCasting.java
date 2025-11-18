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
public class AmateurCasting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String castingImageUrl;

    private String castingImageKeyName;

    private String actorName;

    private String castingName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amateur_show_id")
    private AmateurShow amateurShow;

    public void update(AmateurUpdateRequestDTO.UpdateCasting dto) {
        if (dto.getActorName() != null) this.actorName = dto.getActorName();
        if (dto.getCastingName() != null) this.castingName = dto.getCastingName();
        if (dto.getCastingImageRequestDTO().getImageUrl() != null) this.castingImageUrl = dto.getCastingImageRequestDTO().getImageUrl();
    }

    public void updateCastingImageKeyName(String castingImageUrl) {
        this.castingImageUrl = castingImageUrl;
    }
}
