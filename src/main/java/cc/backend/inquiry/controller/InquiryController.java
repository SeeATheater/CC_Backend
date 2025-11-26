package cc.backend.inquiry.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.inquiry.dto.InquiryRequestDTO;
import cc.backend.inquiry.dto.InquiryResponseDTO;
import cc.backend.inquiry.service.InquiryService;
import cc.backend.member.entity.Member;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "문의")
@RequestMapping("/inquirys")
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("")
    @Operation(summary = "문의 생성하기 API",
            description = "문의를 생성합니다.")
    public ApiResponse<InquiryResponseDTO.CreateInquiryResponseDTO> createInquiry(
            @Valid
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestBody InquiryRequestDTO.CreateInquiryRequestDTO dto) {
        return ApiResponse.onSuccess(inquiryService.createInquiry(member.getId(), dto));
    }

    @GetMapping("/{inquiryId}")
    @Operation(summary = "문의 단건 조회 API",
            description = "문의를 inquiryId로 단건 조회합니다.")
    public ApiResponse<InquiryResponseDTO.InquiryDetailResponseDTO> getInquiryDetail(
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member,
            @PathVariable Long inquiryId) {
        return ApiResponse.onSuccess(inquiryService.getInquiryDetail(member.getId(), inquiryId));
    }

    @GetMapping("")
    @Operation(summary = "문의 리스트 조회 API",
            description = "로그인한 회원이 작성한 문의를 리스트로 조회합니다.")
    public ApiResponse<InquiryResponseDTO.InquiryListResponseDTO> getInquiryList(
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기(0부터 시작)", example = "20")
            @RequestParam(defaultValue = "20")  int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ApiResponse.onSuccess(inquiryService.getInquiryList(member.getId(), pageable));
    }

    @DeleteMapping("/{inquiryId}")
    @Operation(summary = "문의 삭제하기 API",
            description = "문의를 inquiryId로 삭제합니다.")
    public ApiResponse<Void> deleteInquiry(
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member,
            @PathVariable Long inquiryId) {
        inquiryService.deleteInquiry(member.getId(), inquiryId);
        return ApiResponse.onSuccess(null);
    }


}
