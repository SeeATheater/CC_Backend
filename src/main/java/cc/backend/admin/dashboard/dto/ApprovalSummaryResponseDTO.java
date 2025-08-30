package cc.backend.admin.dashboard.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalSummaryResponseDTO {
    private Long showId;
    private String showName;
    private String dateTime;
    private int capacity;
}
