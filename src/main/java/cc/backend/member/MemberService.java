package cc.backend.member;


import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.member.dto.MyPageResponseDTO;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.ActiveStatus;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MyPageResponseDTO getMyPage(Long  memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        return MyPageResponseDTO.builder()
                .id(memberId)
                .name(member.getName())
                .username(member.getUsername())
                .phone(member.getPhone())
                .email(member.getEmail())
                .address(member.getAddress())
                .status(member.getActive_status())
                .build();
    }

    @Transactional
    public MyPageResponseDTO deactivateMember(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (!member.getActive_status().equals(ActiveStatus.ACTIVE)) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_DEACTIVATED);
        }
        member.deactivateMember(member);
        return MyPageResponseDTO.builder()
                .id(memberId)
                .phone(member.getPhone())
                .email(member.getEmail())
                .name(member.getName())
                .username(member.getUsername())
                .address(member.getAddress())
                .status(member.getActive_status()).build();

    }

    @Transactional
    public MyPageResponseDTO reactivateMember(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (!member.getActive_status().equals(ActiveStatus.INACTIVE)) {
            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_ACTIVATED);
        }
        member.reactivateMember(member);
        return MyPageResponseDTO.builder()
                .id(memberId)
                .phone(member.getPhone())
                .email(member.getEmail())
                .name(member.getName())
                .username(member.getUsername())
                .address(member.getAddress())
                .status(member.getActive_status()).build();

    }

}
