package cc.backend.service.amateurShowService;

import cc.backend.domain.entity.member.Member;
import cc.backend.dto.amateurDTO.AmateurEnrollRequestDTO;
import cc.backend.dto.amateurDTO.AmateurEnrollResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface AmateurService {
    AmateurEnrollResponseDTO.AmateurEnrollResult enrollShow(Member member,
                                                                AmateurEnrollRequestDTO requestDTO,
                                                                MultipartFile posterImage,
                                                                List<MultipartFile> castingImages,
                                                                List<MultipartFile> noticeImages);
}