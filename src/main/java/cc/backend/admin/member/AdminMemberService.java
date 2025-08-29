package cc.backend.admin.member;

import cc.backend.admin.member.dto.AdminMemberListResponseDTO;
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

    public Slice<AdminMemberListResponseDTO> getMemberList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<Member> pageResult = memberRepository.findAll(pageable);

        List<AdminMemberListResponseDTO> content = pageResult.getContent().stream()
                .map(this::toDto)
                .toList();

        return new SliceImpl<>(content, pageable, pageResult.hasNext());
    }

    private AdminMemberListResponseDTO toDto(Member m) {
        return AdminMemberListResponseDTO.builder()
                .memberId(m.getId())
                .userName(m.getUsername())  // 엔티티 필드명에 맞게 필요 시 수정
                .name(m.getName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .build();
    }


}
