package cc.backend.amateurShow.converter;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.entity.*;
import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.member.entity.Member;

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
                .place(requestDTO.getPlace())
                .schedule(requestDTO.getSchedule())
                .runtime(requestDTO.getRuntime())
                .summary(requestDTO.getSummary())
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

        return castings.stream().map(casting -> AmateurCasting.builder()
                        .amateurShow(amateurShow)
                        .actorName(casting.getActorName())
                        .castingName(casting.getCastingName())
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

    public static List<AmateurTicket> toAmateurTicketEntity(AmateurEnrollRequestDTO requestDTO,
                                                            AmateurShow amateurShow) {
        if (requestDTO.getTickets() == null) return Collections.emptyList();

        return requestDTO.getTickets().stream()
                .map(t -> AmateurTicket.builder()
                        .amateurShow(amateurShow)
                        .discountName(
                                (t.getDiscountName() == null || t.getDiscountName().isBlank())
                                        ? "COMMON" : t.getDiscountName()
                        )
                        .price(t.getPrice())
                        .build())
                .collect(Collectors.toList());
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
                        .totalTicket(r.getTotalTicket())
                        .amateurShow(show)
                        .build())
                .collect(Collectors.toList());
    }

    // --소극장 공연 단건 조회 response--
    public static AmateurShowResponseDTO.AmateurShowResult toResponseDTO(AmateurShow amateurShow) {

        List<AmateurShowResponseDTO.AmateurShowResult.Tickets> tickets = amateurShow.getAmateurTicketList().stream()
                .map(t -> AmateurShowResponseDTO.AmateurShowResult.Tickets.builder()
                        .discountName(t.getDiscountName())
                        .price(t.getPrice())
                        .build())
                .toList();

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
                .place(amateurShow.getPlace())
                .runtime(amateurShow.getRuntime())
                .account(amateurShow.getAccount())
                .contact(amateurShow.getContact())
                .hashtag(amateurShow.getHashtag())
                .summary(amateurShow.getSummary())
                .noticeContent(amateurShow.getAmateurNotice() != null ? amateurShow.getAmateurNotice().getContent() : null)
                .tickets(tickets)
                .casting(castings)
                .staff(staff)
                .rounds(rounds)
                .build();
    }
}
