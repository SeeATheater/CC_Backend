//package cc.backend.admin.adminMyPage.service;
//
//import cc.backend.admin.adminMyPage.dto.AdminAccountDetailDTO;
//import cc.backend.admin.adminMyPage.dto.AdminChangePasswordRequestDTO;
//import cc.backend.admin.adminMyPage.dto.AdminChangePasswordResponseDTO;
//import cc.backend.admin.adminMyPage.entity.Admin;
//import cc.backend.admin.adminMyPage.repository.AdminRepository;
//import cc.backend.apiPayLoad.code.status.ErrorStatus;
//import cc.backend.apiPayLoad.exception.GeneralException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class AdminMyPageService {
//
//    private final AdminRepository adminRepository;
//
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    public AdminChangePasswordResponseDTO changePassword(AdminChangePasswordRequestDTO requestDTO) {
//
//        if (!requestDTO.getNewPassword().equals(requestDTO.getCheckNewPassword())) {
//            throw new GeneralException(ErrorStatus.PASSWORD_NOT_MATCH);
//        }
//
////        Admin currentAdmin = adminRepository.findById(adminId)
////                .orElseThrow(() -> new GeneralException(ErrorStatus.ADMIN_NOT_FOUND)); // (ErrorStatus에 추가 필요)
//
//        currentAdmin.updatePassword(bCryptPasswordEncoder.encode(requestDTO.getNewPassword()));
//
//
//        return AdminChangePasswordResponseDTO.builder()
//                .adminId(currentAdmin.getId())
//                .message("비밀번호가 성공적으로 변경되었습니다.")
//                .changedAt(LocalDateTime.now())
//                .build();
//    }
//
//    public AdminAccountDetailDTO getAdminAccountDetail(Long id) {
//        Admin currentAdmin = adminRepository.findById(id)
//               .orElseThrow(() -> new GeneralException(ErrorStatus.ADMIN_NOT_FOUND)); // (ErrorStatus에 추가 필요)
//
//        return AdminAccountDetailDTO.builder()
//                .id(id)
//                .adminId(currentAdmin.getAdminId())
//                .accountNumber(currentAdmin.getAccountNumber()).build();
//    }
//}
