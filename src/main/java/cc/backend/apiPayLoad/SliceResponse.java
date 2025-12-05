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

    @Schema(description = "현재 페이지 번호 (0부터 시작)")
    private int page;

    @Schema(description = "페이지 크기")
    private int size;

    @Schema(description = "첫 번째 페이지 여부")
    private boolean first;

    @Schema(description = "마지막 페이지 여부")
    private boolean last;

    @Schema(description = "다음 페이지 존재 여부")
    private boolean hasNext;

    public static <T> SliceResponse<T> of(Slice<T> slice) {
        return SliceResponse.<T>builder()
                            .content(slice.getContent())
                            .page(slice.getNumber())
                            .size(slice.getSize())
                            .first(slice.isFirst())
                            .last(slice.isLast())
                            .hasNext(slice.hasNext())
                            .build();
    }
}