package cc.backend.ticket.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.*;
import cc.backend.ticket.dto.request.TempTicketCreateRequestDTO;
import cc.backend.ticket.service.TempTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "소극장 공연 티켓")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
@Validated
public class TempTicketController {

    private final TempTicketService tempTicketService;

    @GetMapping("/{amateurShowId}/showSimple")
    @Operation(
            summary = "소극장 공연 티켓 예매 - 공연 정보 간략 보기 API",
            description = "소극장 공연 티켓 예매 동안 보이는 모든 (화면 공연 사진, 공연 제목, 공연 장소)에 대해 나옵니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                            description = "공연 간략 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = AmateurShowSimpleDTO.class)))
            }
    )
    public ApiResponse<AmateurShowSimpleDTO> getSimpleAmateurShow(@Parameter(name = "amateurShowId", description = "공연 ID", required = true) @PathVariable Long amateurShowId) {
        return ApiResponse.onSuccess(tempTicketService.getSimpleAmateurShow(amateurShowId));
    }

    @GetMapping("/{amateurShowId}/selectRound")
    @Operation(
            summary = "소극장 공연 티켓 예매 첫화면 - 회차(날짜) 선택 API",
            description = "소극장 공연 티켓 예매 첫화면에서 공연 회차를 선택하기전 조회하는 기능입니다. 등록된 공연의 모든 회차가 조회 됩니다.",
            parameters = {
                    @Parameter(name = "amateurShowId", description = "공연 ID", required = true),
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회차 조회 성공",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = RoundsListDTO.class)
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공연을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    public ApiResponse<List<RoundsListDTO>> getAmateurRounds(@PathVariable Long amateurShowId,
                                                             @AuthenticationPrincipal(expression = "member") Member member){
        return ApiResponse.onSuccess(tempTicketService.getRoundsList(member.getId(), amateurShowId));
    }

    @GetMapping("/{amateurShowId}/selectTicket")
    @Operation(
            summary = "소극장 공연 티켓 예매 두번째 화면 - 티켓 종류 선택 API",
            description = "소극장 공연 티켓 예매 두번째화면에서 티켓 종류를 선택하기 전 조회하는 기능입니다. 등록된 공연의 모든 티켓이 조회됩니다.",
            parameters = {
                    @Parameter(name = "amateurShowId", description = "공연 ID", required = true),
            }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "티켓 종류 조회 성공",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = AmateurTicketListDTO.class))
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공연을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ApiResponse<List<AmateurTicketListDTO>> getAmateurTicketList(@PathVariable Long amateurShowId,
                                                                    @AuthenticationPrincipal(expression = "member") Member member){

        return ApiResponse.onSuccess(tempTicketService.getAmateurTicketList(member.getId(), amateurShowId));

    }

    @PostMapping("/{amateurShowId}/reserve")
    @Operation(summary = "소극장 공연 티켓 생성 API",
    description = "소극장 공연 티켓을 생성하는 기능입니다. 공연 회차, 인원, 티켓 종류를 선택해 생성합니다.",
            parameters = {
                    @Parameter(name = "amateurShowId", description = "공연 ID", required = true),
                    @Parameter(name = "amateurRoundId", description = "회차 ID", required = true),
                    @Parameter(name = "amateurTicketId", description = "티켓 ID", required = true),
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "티켓 예매 성공",
                            content = @Content(
                                    schema = @Schema(implementation = TempTicketCreateResponseDTO.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "공연 정보 불일치 또는 예매 가능 수량 초과",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "공연, 회차 또는 티켓 정보를 찾을 수 없음",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 (인증되지 않은 사용자)",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ApiResponse<TempTicketCreateResponseDTO> createTempTicket(
            @PathVariable @Positive(message = "_BAD_REQUEST") Long amateurShowId,
            @RequestParam @Positive(message = "_BAD_REQUEST") Long amateurRoundId,
            @RequestParam @Positive(message = "_BAD_REQUEST") Long amateurTicketId,
            @AuthenticationPrincipal(expression = "member") Member member,
            @Valid @RequestBody TempTicketCreateRequestDTO requestDTO) {
        return ApiResponse.onSuccess(
                tempTicketService.createTempTicket(amateurShowId, amateurRoundId, amateurTicketId, member, requestDTO)
        );
    }




}
