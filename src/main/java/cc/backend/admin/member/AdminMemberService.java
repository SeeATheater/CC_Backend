package cc.backend.admin.member;

import cc.backend.admin.member.dto.AdminMemberDetailResponseDTO;
import cc.backend.admin.member.dto.AdminMemberListResponseDTO;
import cc.backend.admin.member.dto.UpdateMemberDetailRequestDTO;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public Slice<AdminMemberListResponseDTO> getMemberList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Member> pageResult;

        if (keyword != null && !keyword.isBlank()) {
            pageResult = memberRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
        } else {
            pageResult = memberRepository.findAll(pageable);
        }

        List<AdminMemberListResponseDTO> content = pageResult.getContent().stream()
                .map(this::toDto)
                .toList();

        return new SliceImpl<>(content, pageable, pageResult.hasNext());
    }

    private AdminMemberListResponseDTO toDto(Member m) {
        return AdminMemberListResponseDTO.builder()
                .memberId(m.getId())
                .username(m.getUsername())  // 엔티티 필드명에 맞게 필요 시 수정
                .name(m.getName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .build();
    }

    public AdminMemberDetailResponseDTO getMemberDetail(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return toDetailDTO(member);
    }

    private AdminMemberDetailResponseDTO toDetailDTO(Member m){
        return AdminMemberDetailResponseDTO.builder()
                .username(m.getUsername())
                .name(m.getName())
                .phone(m.getPhone())
                .email(m.getEmail())
                .birth_date(m.getBirth_date())
                .gender(m.getGender())
                .address(m.getAddress())
                .build();
    }

    @Transactional
    public AdminMemberDetailResponseDTO updateMemberDetail(Long memberId, UpdateMemberDetailRequestDTO dto){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateMemberDetail(
                dto.getUsername(),
                dto.getName(),
                dto.getPhone(),
                dto.getEmail(),
                dto.getBirth_date(),
                dto.getGender(),
                dto.getAddress()
        );

        return toDetailDTO(member);

    }



}
