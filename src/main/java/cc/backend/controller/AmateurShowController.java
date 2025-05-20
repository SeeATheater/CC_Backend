package cc.backend.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.domain.entity.member.Member;
import cc.backend.dto.amateurDTO.AmateurEnrollRequestDTO;
import cc.backend.dto.amateurDTO.AmateurEnrollResponseDTO;
import cc.backend.service.amateurShowService.AmateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/amateurs")
public class AmateurShowController {

    private final MemberService memberService;
    private final AmateurService amateurService;

    // 소극장 공연 등록
    @PostMapping
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> enrollShow(@RequestHeader("Authorization") String authorizationHeader,
                                                                                    @RequestPart("data") AmateurEnrollRequestDTO requestDTO,
                                                                                    @RequestPart(name = "posterImage", required = false) MultipartFile posterImage,
                                                                                    @RequestPart(name = "castingImages", required = false)List<MultipartFile> castingImages,
                                                                                    @RequestPart(name = "noticeImages", required = false) List<MultipartFile> noticeImages) {
        Member member = memberService.getMemberByToken(authorizationHeader);

        AmateurEnrollResponseDTO.AmateurEnrollResult enroll = amateurService.enrollShow(member, requestDTO, posterImage, castingImages, noticeImages);
        return ApiResponse.onSuccess(enroll);
    }


}
