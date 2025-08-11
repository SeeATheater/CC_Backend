package cc.backend.admin.amateurShow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAmateurShowReviseRequestDTO {
    private String hashtag;
    private String summary;
    private String account;
    private String contact;
}
