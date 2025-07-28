package cc.backend.member;


import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.board.dto.response.BoardDetailResponse;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.board.service.BoardService;
import cc.backend.member.dto.MyPageResponseDTO;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "회원 마이페이지", description = "마이페이지 조회 및 회원 활성/비활성화 API")
public class MemberController {

    private final MemberService memberService;
    private final BoardService boardService;

    @GetMapping("/myPage")
    @Operation(
            summary = "마이 페이지 조회 API",
            description = "현재 로그인한 회원의 마이 페이지 정보를 조회합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "마이페이지 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyPageResponseDTO.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "MEMBER4000 - 존재하지 않는 사용자입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorStatus.class))
                    )
            }
    )
    public ApiResponse<MyPageResponseDTO> myPage(
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member
    ) {
        return ApiResponse.onSuccess(memberService.getMyPage(member.getId()));
    }

    @PatchMapping("/myPage/deActive")
    @Operation(
            summary = "회원 탈퇴(비활성화) API",
            description = "현재 로그인한 회원의 계정을 비활성화합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원 비활성화 성공",
                            content = @Content(schema = @Schema(implementation = MyPageResponseDTO.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "MEMBER4000 - 존재하지 않는 사용자입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorStatus.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "MEMBER4012 - 해당 회원은 이미 탈퇴(비활성화) 상태입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorStatus.class))
                    )
            }
    )
    public ApiResponse<MyPageResponseDTO> deactivateMember(
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member
    ) {
        return ApiResponse.onSuccess(memberService.deactivateMember(member.getId()));
    }

    @PatchMapping("/myPage/reActive")
    @Operation(
            summary = "회원 활성화 API",
            description = "현재 로그인한 회원의 계정을 다시 활성화합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원 활성화 성공",
                            content = @Content(schema = @Schema(implementation = MyPageResponseDTO.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "MEMBER4000 - 존재하지 않는 사용자입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorStatus.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "MEMBER4013 - 해당 회원은 이미 활성화 상태입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorStatus.class))
                    )
            }
    )
    public ApiResponse<MyPageResponseDTO> reactivateMember(
            @Parameter(description = "로그인된 회원 정보", hidden = true)
            @AuthenticationPrincipal(expression = "member") Member member
    ) {
        return ApiResponse.onSuccess(memberService.reactivateMember(member.getId()));
    }


    @Operation(summary = "게시글 목록 조회 API", description = "게시글을 무한 스크롤 방식으로 조회합니다.")
    @GetMapping("/myPage/myBoard")
    public Slice<BoardDetailResponse> getMyBoards(
            @Parameter(description = "작성자 회원 ID", required = true) @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "페이지 번호(0부터 시작)", required = true) @RequestParam int page,
            @Parameter(description = "페이지 크기", required = true) @RequestParam int size
    ) {
        return boardService.getMyBoards(member, page, size);
    }

}