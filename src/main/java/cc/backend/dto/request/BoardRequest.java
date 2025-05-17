package cc.backend.dto.request;

import cc.backend.entity.enums.BoardType;
import lombok.Getter;

import java.util.List;

@Getter
public class BoardRequest {
    private String title;
    private String content;
    private List<String> imgUrls;
    private BoardType boardType;
}
