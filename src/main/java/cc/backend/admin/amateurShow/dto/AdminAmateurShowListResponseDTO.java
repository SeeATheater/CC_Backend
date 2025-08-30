package cc.backend.admin.amateurShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminAmateurShowListResponseDTO {
    private Long showId;
    private String showName;
    private LocalDateTime createdAt;
    private String performerName;
    private String amateurShowStatus;

}
