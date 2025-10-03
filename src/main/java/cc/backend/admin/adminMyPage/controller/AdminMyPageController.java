//package cc.backend.admin.adminMyPage.controller;
//
//import cc.backend.admin.adminMyPage.dto.AdminAccountDetailDTO;
//import cc.backend.admin.adminMyPage.dto.AdminChangePasswordRequestDTO;
//import cc.backend.admin.adminMyPage.dto.AdminChangePasswordResponseDTO;
//import cc.backend.admin.adminMyPage.service.AdminMyPageService;
//import cc.backend.apiPayLoad.ApiResponse;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/admin")
//@Tag(name = "관리자 소극장 티켓 관리")
//public class AdminMyPageController {
//
//    public final AdminMyPageService adminMyPageService;
//
//    @PostMapping("/change-password")
//    @Operation(summary = "관리자 비밀번호 변경 API")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<AdminChangePasswordResponseDTO> changePassword(
//            @RequestBody AdminChangePasswordRequestDTO requestDTO
//    ) {
//        return ApiResponse.onSuccess(adminMyPageService.changePassword(requestDTO));
//    }
//
//    @GetMapping("/account")
//    @Operation(summary = "관리자 결제수단 조회 API")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ApiResponse<AdminAccountDetailDTO> getAccountDetails(){
//        return ApiResponse.onSuccess(adminMyPageService.getAccountDetails());
//    }
//
//
//}
