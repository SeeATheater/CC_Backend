package cc.backend.admin.adminMyPage.controller;

import cc.backend.admin.adminMyPage.dto.AdminAuthDTO;
import cc.backend.admin.adminMyPage.dto.AdminChangePasswordRequestDTO;
import cc.backend.admin.adminMyPage.dto.AdminChangePasswordResponseDTO;
import cc.backend.admin.adminMyPage.service.AdminMyPageService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.config.jwt.dto.TokenDTO;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@Tag(name = "관리자 마이페이지")
public class AdminMyPageController {

    public final AdminMyPageService adminMyPageService;

    @PostMapping("/change-password")
    @Operation(summary = "관리자 비밀번호 변경 API")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminChangePasswordResponseDTO> changePassword(
            @RequestBody AdminChangePasswordRequestDTO requestDTO
    ) {
        return ApiResponse.onSuccess(adminMyPageService.changePassword(requestDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "관리자 로그인 API",
    description = "비밀번호를 사용해 관리자 로그인을 합니다.")
    public ApiResponse<TokenDTO> login(
            @RequestBody AdminAuthDTO.LoginRequest dto
            ){
        return ApiResponse.onSuccess(adminMyPageService.login(dto));    }


}
