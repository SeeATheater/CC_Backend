package cc.backend.admin.adminMyPage.service;

import cc.backend.admin.adminMyPage.dto.AdminAuthDTO;
import cc.backend.admin.adminMyPage.dto.AdminChangePasswordRequestDTO;
import cc.backend.admin.adminMyPage.dto.AdminChangePasswordResponseDTO;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.config.jwt.TokenProvider;
import cc.backend.config.jwt.dto.TokenDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminMyPageService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

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

    public TokenDTO login(AdminAuthDTO.LoginRequest req) {
        Member member = memberRepository.findMemberByEmail(req.getEmail())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (member.getRole() != Role.ADMIN) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_AUTHORIZED);
        }

        if (!passwordEncoder.matches(req.getPassword(), member.getPassword())) {
            throw new GeneralException(ErrorStatus.PASSWORD_NOT_MATCH);
        }


        return tokenProvider.generateTokenDto(member);
    }
}
