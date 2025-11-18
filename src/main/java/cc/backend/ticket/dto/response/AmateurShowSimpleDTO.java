package cc.backend.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AmateurShowSimpleDTO {
    private Long amateurShowId;
    private String name;
    //private String place;
    private String detailAddress;
    private String posterKeyName;
}
