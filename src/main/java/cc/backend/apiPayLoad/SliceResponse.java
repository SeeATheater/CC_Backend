package cc.backend.apiPayLoad;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliceResponse<T> {

    @Schema(description = "데이터 리스트")
    private List<T> content;

    @Schema(description = "페이지 크기")
    private int pageSize;

    @Schema(description = "다음 페이지 존재 여부")
    private boolean hasNext;

    public static <T> SliceResponse<T> of(Slice<T> slice) {
        return SliceResponse.<T>builder()
                            .content(slice.getContent())
                            .pageSize(slice.getSize())
                            .hasNext(slice.hasNext())
                            .build();
    }
}