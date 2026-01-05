package cc.backend.kakaoPay.dto.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoPayResultResponseDTO {
    private Long amateurShowId;
    private KakaoPayApproveResponseDTO approveResponse;
}
