package cc.backend.inquiry.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.inquiry.converter.InquiryRequestConverter;
import cc.backend.inquiry.converter.InquiryResponseConverter;
import cc.backend.inquiry.dto.InquiryRequestDTO;
import cc.backend.inquiry.dto.InquiryResponseDTO;
import cc.backend.inquiry.entity.Inquiry;
import cc.backend.inquiry.repository.InquiryRepository;
import cc.backend.member.entity.Member;
import cc.backend.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public InquiryResponseDTO.CreateInquiryResponseDTO createInquiry(Long memberId, InquiryRequestDTO.CreateInquiryRequestDTO inquiryRequestDTO) {
        Member member = memberRepository.findById(memberId).orElseThrow(()->new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Inquiry inquiry = InquiryRequestConverter.toEntity(member, inquiryRequestDTO);

        inquiryRepository.save(inquiry);
        return InquiryResponseConverter.toDTO(inquiry);
    }


    public InquiryResponseDTO.InquiryDetailResponseDTO getInquiryDetail(Long memberId, Long inquiryId) {
        Member member = memberRepository.findById(memberId).orElseThrow(()->new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(()->new GeneralException(ErrorStatus.INQUIRY_NOT_FOUND));
        if(!inquiry.getMember().getId().equals(member.getId())) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_INQUIRY_ACCESS);
        }

        return InquiryResponseConverter.toDetailDTO(inquiry);
    }



}
