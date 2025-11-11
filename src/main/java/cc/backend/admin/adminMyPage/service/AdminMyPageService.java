package cc.backend.admin.adminMyPage.service;

import cc.backend.admin.adminMyPage.dto.AdminChangePasswordRequestDTO;
import cc.backend.admin.adminMyPage.dto.AdminChangePasswordResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminMyPageService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AdminChangePasswordResponseDTO changePassword(AdminChangePasswordRequestDTO requestDTO) {

        if (!requestDTO.getNewPassword().equals(requestDTO.getCheckNewPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_NOT_MATCH);
        }

        Member currentAdmin = memberRepository.findByRole(Role.ADMIN)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ADMIN_NOT_FOUND)); // (ErrorStatus에 추가 필요)

        currentAdmin.updatePassword(bCryptPasswordEncoder.encode(requestDTO.getNewPassword()));


        return AdminChangePasswordResponseDTO.builder()
                .memberId(currentAdmin.getId())
                .changedAt(LocalDateTime.now())
                .build();
    }


}
