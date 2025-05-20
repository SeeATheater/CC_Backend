package cc.backend.amateurShow.converter;

import cc.backend.amateurShow.entity.*;
import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.entity.enums.TicketType;
import cc.backend.member.entity.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AmateurConverter {

    // 소극장 공연 생성
    public static AmateurShow toAmateurShowEntity(Member member, AmateurEnrollRequestDTO requestDTO) {
        return AmateurShow.builder()
                .member(member)
                .name(requestDTO.getName())
                .troupe(requestDTO.getTroupe())
                .place(requestDTO.getPlace())
                .schedule(requestDTO.getSchedule())
                .runtime(requestDTO.getRuntime())
                .age(requestDTO.getAge())
                .starring(requestDTO.getStarring())
                .totalTicket(requestDTO.getTotalTicket())
                .timeInfo(requestDTO.getTimeInfo())
                .account(requestDTO.getAccount())
                .contact(requestDTO.getContact())
                .hashtag(requestDTO.getHashtag())
                //.posterImgUrl(posterUrl)
                .soldTicket(0)
                .performanceRounds(requestDTO.getPerformanceRounds())
                .build();
    }

    // 소극장 공연 생성 response
    public static AmateurEnrollResponseDTO.AmateurEnrollResult toAmateurShowDTO(AmateurShow amateurShow) {
        return AmateurEnrollResponseDTO.AmateurEnrollResult.builder()
                .id(amateurShow.getId())
                .name(amateurShow.getName())
                .build();
    }

    // 엔티티로 변환
    public static List<AmateurCasting> toAmateurCastingEntity(List<AmateurEnrollRequestDTO.Casting> castings,
                                                              AmateurShow amateurShow) {
        if (castings == null || castings.isEmpty()) return Collections.emptyList();

        return IntStream.range(0, castings.size())
                .mapToObj(i -> AmateurCasting.builder()
                        .amateurShow(amateurShow)
                        //.imageUrl(castingUrls.get(i))
                        .actorName(castings.get(i).getActorName())
                        .castingName(castings.get(i).getCastingName())
                        .build())
                .collect(Collectors.toList());
    }

    public static AmateurNotice toAmateurNoticeEntity(String noticeContent, AmateurShow amateurShow) {
        if (noticeContent == null) return null;

        return AmateurNotice.builder()
                .amateurShow(amateurShow)
                //.noticeImageUrls(noticeUrls)
                .content(noticeContent)
                .build();
    }

    public static AmateurSummary toAmateurSummaryEntity(String summaryContent,
                                                        AmateurShow amateurShow) {
        if (summaryContent == null) return null;

        return AmateurSummary.builder()
                .amateurShow(amateurShow)
                .content(summaryContent)
                .build();
    }

    public static List<AmateurTicket> toAmateurTicketEntity(AmateurEnrollRequestDTO requestDTO,
                                                            AmateurShow amateurShow) {
        List<AmateurTicket> tickets = new ArrayList<>();

        // 일반 티켓
        if (requestDTO.getRegularTicket() != null && !requestDTO.getRegularTicket().isEmpty()) {
            for (AmateurEnrollRequestDTO.RegularTicket regularTicket : requestDTO.getRegularTicket()) {
                AmateurTicket ticket = AmateurTicket.builder()
                        .amateurShow(amateurShow)
                        .ticketType(TicketType.COMMON)
                        .price(regularTicket.getRegularPrice())
                        .discountName(null)
                        .build();
                tickets.add(ticket);
            }
        }

        // 할인 티켓
        if (requestDTO.getDiscountTicket() != null && !requestDTO.getDiscountTicket().isEmpty()) {
            for (AmateurEnrollRequestDTO.DiscountTicket discountTicket : requestDTO.getDiscountTicket()) {
                AmateurTicket ticket = AmateurTicket.builder()
                        .amateurShow(amateurShow)
                        .ticketType(TicketType.DISCOUNT)
                        .price(discountTicket.getDiscountPrice())
                        .discountName(discountTicket.getDiscountName())
                        .build();
                tickets.add(ticket);
            }
        }

        return tickets;
    }

    public static List<AmateurStaff> toAmateurStaffEntity(List<AmateurEnrollRequestDTO.Staff> staffs,
                                                          AmateurShow amateurShow) {
        if (staffs == null || staffs.isEmpty()) return Collections.emptyList();

        return staffs.stream()
                .map(staff -> AmateurStaff.builder()
                        .amateurShow(amateurShow)
                        .position(staff.getPosition())
                        .name(staff.getStaffName())
                        .build())
                .collect(Collectors.toList());
    }
}
