package cc.backend.notice.service;

import cc.backend.notice.dto.NoticeResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface NoticeService {
    public NoticeResponseDTO.BoardNoticeResultDTO notifyHotBoard(Long boardId);
    public NoticeResponseDTO.BoardNoticeResultDTO notifyCommentBoard(Long boardId);
}
