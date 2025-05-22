package cc.backend.amateurShow.converter;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
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

    // --소극장 공연 생성--
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
                .soldTicket(0)
                .build();
    }

    // --소극장 공연 생성 response--
    public static AmateurEnrollResponseDTO.AmateurEnrollResult toAmateurEnrollDTO(AmateurShow amateurShow) {
        return AmateurEnrollResponseDTO.AmateurEnrollResult.builder()
                .id(amateurShow.getId())
                .name(amateurShow.getName())
                .build();
    }

    public static List<AmateurCasting> toAmateurCastingEntity(List<AmateurEnrollRequestDTO.Casting> castings,
                                                              AmateurShow amateurShow) {
        if (castings == null || castings.isEmpty()) return Collections.emptyList();

        return IntStream.range(0, castings.size())
                .mapToObj(i -> AmateurCasting.builder()
                        .amateurShow(amateurShow)
                        .actorName(castings.get(i).getActorName())
                        .castingName(castings.get(i).getCastingName())
                        .build())
                .collect(Collectors.toList());
    }

    public static AmateurNotice toAmateurNoticeEntity(String noticeContent, AmateurShow amateurShow) {
        if (noticeContent == null) return null;

        return AmateurNotice.builder()
                .amateurShow(amateurShow)
                .content(noticeContent)
                .build();
    }

    public static AmateurSummary toAmateurSummaryEntity(String summaryContent, AmateurShow amateurShow) {
        if (summaryContent == null) return null;

        return AmateurSummary.builder()
                .amateurShow(amateurShow)
                .content(summaryContent)
                .build();
    }

    public static List<AmateurTicket> toAmateurTicketEntity(AmateurEnrollRequestDTO requestDTO,
                                                            AmateurShow amateurShow) {
        List<AmateurTicket> tickets = new ArrayList<>();

        if (requestDTO.getRegularTicket() != null) {
            for (AmateurEnrollRequestDTO.RegularTicket rt : requestDTO.getRegularTicket()) {
                tickets.add(AmateurTicket.builder()
                        .amateurShow(amateurShow)
                        .ticketType(TicketType.COMMON)
                        .price(rt.getRegularPrice())
                        .discountName(null)
                        .build());
            }
        }

        if (requestDTO.getDiscountTicket() != null) {
            for (AmateurEnrollRequestDTO.DiscountTicket dt : requestDTO.getDiscountTicket()) {
                tickets.add(AmateurTicket.builder()
                        .amateurShow(amateurShow)
                        .ticketType(TicketType.DISCOUNT)
                        .price(dt.getDiscountPrice())
                        .discountName(dt.getDiscountName())
                        .build());
            }
        }

        return tickets;
    }

    public static List<AmateurStaff> toAmateurStaffEntity(List<AmateurEnrollRequestDTO.Staff> staffs,
                                                          AmateurShow amateurShow) {
        if (staffs == null || staffs.isEmpty()) return Collections.emptyList();

        return staffs.stream()
                .map(s -> AmateurStaff.builder()
                        .amateurShow(amateurShow)
                        .position(s.getPosition())
                        .name(s.getStaffName())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<AmateurRounds> toAmateurRoundEntity(List<AmateurEnrollRequestDTO.Rounds> rounds,
                                                           AmateurShow show) {
        return rounds.stream()
                .map(r -> AmateurRounds.builder()
                        .roundNumber(r.getRoundNumber())
                        .performanceDateTime(r.getPerformanceDateTime())
                        .amateurShow(show)
                        .build())
                .collect(Collectors.toList());
    }

    // --소극장 공연 단건 조회 response--
    public static AmateurShowResponseDTO.AmateurShowResult toResponseDTO(AmateurShow amateurShow) {
        List<AmateurShowResponseDTO.AmateurShowResult.RegularTicket> regularTickets = amateurShow.getAmateurTicketList().stream()
                .filter(t -> t.getTicketType() == TicketType.COMMON)
                .map(t -> AmateurShowResponseDTO.AmateurShowResult.RegularTicket.builder()
                        .regularPrice(t.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.DiscountTicket> discountTickets = amateurShow.getAmateurTicketList().stream()
                .filter(t -> t.getTicketType() == TicketType.DISCOUNT)
                .map(t -> AmateurShowResponseDTO.AmateurShowResult.DiscountTicket.builder()
                        .discountName(t.getDiscountName())
                        .discountPrice(t.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.Casting> castings = amateurShow.getAmateurCastingList().stream()
                .map(c -> AmateurShowResponseDTO.AmateurShowResult.Casting.builder()
                        .actorName(c.getActorName())
                        .castingName(c.getCastingName())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.Staff> staff = amateurShow.getAmateurStaffList().stream()
                .map(s -> AmateurShowResponseDTO.AmateurShowResult.Staff.builder()
                        .position(s.getPosition())
                        .staffName(s.getName())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.Rounds> rounds = amateurShow.getAmateurRounds().stream()
                .map(r -> AmateurShowResponseDTO.AmateurShowResult.Rounds.builder()
                        .roundNumber(r.getRoundNumber())
                        .performanceDateTime(r.getPerformanceDateTime())
                        .build())
                .collect(Collectors.toList());

        return AmateurShowResponseDTO.AmateurShowResult.builder()
                .amateurShowId(amateurShow.getId())
                .name(amateurShow.getName())
                .troupe(amateurShow.getTroupe())
                .place(amateurShow.getPlace())
                .schedule(amateurShow.getSchedule())
                .runtime(amateurShow.getRuntime())
                .age(amateurShow.getAge())
                .starring(amateurShow.getStarring())
                .totalTicket(amateurShow.getTotalTicket())
                .timeInfo(amateurShow.getTimeInfo())
                .account(amateurShow.getAccount())
                .contact(amateurShow.getContact())
                .hashtag(amateurShow.getHashtag())
                .summaryContent(amateurShow.getAmateurSummary() != null ? amateurShow.getAmateurSummary().getContent() : null)
                .noticeContent(amateurShow.getAmateurNotice() != null ? amateurShow.getAmateurNotice().getContent() : null)
                .regularTicket(regularTickets)
                .discountTicket(discountTickets)
                .casting(castings)
                .staff(staff)
                .rounds(rounds)
                .build();
    }
}
