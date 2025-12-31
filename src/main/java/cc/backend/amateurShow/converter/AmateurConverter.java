package cc.backend.amateurShow.converter;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.amateurShow.entity.*;
import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.member.entity.Member;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AmateurConverter {

    // --소극장 공연 생성--
    public static AmateurShow toAmateurShowEntity(Member member, AmateurEnrollRequestDTO requestDTO) {

        return AmateurShow.builder()
                .member(member)
                .name(requestDTO.getName())
                .performerName(member.getName()) // 등록자 이름으로 기본 설정
                .hallName(requestDTO.getHallName())
                .roadAddress(requestDTO.getRoadAddress())
                .detailAddress(requestDTO.getDetailAddress())
                .start(requestDTO.getStart())
                .end(requestDTO.getEnd())
                .posterImageUrl(requestDTO.getPosterImageRequestDTO().getImageUrl() != null ?
                        requestDTO.getPosterImageRequestDTO().getImageUrl() : null)
                .runtime(requestDTO.getRuntime())
                .summary(requestDTO.getSummary())
                .account(requestDTO.getAccount())
                .bankName(requestDTO.getBankName())
                .depositor(requestDTO.getDepositor())
                .contact(requestDTO.getContact())
                .hashtag(requestDTO.getHashtag())
                .build();
    }

    // --소극장 공연 생성 response--
    public static AmateurEnrollResponseDTO.AmateurEnrollResult toAmateurEnrollDTO(AmateurShow amateurShow) {


        return AmateurEnrollResponseDTO.AmateurEnrollResult.builder()
                .amateurShowId(amateurShow.getId())
                .name(amateurShow.getName())
                .memberId(amateurShow.getMember().getId())
                .build();
    }

    public static List<AmateurCasting> toAmateurCastingEntity(List<AmateurEnrollRequestDTO.Casting> castings,
                                                              AmateurShow amateurShow) {
        if (castings == null || castings.isEmpty()) return Collections.emptyList();

        return castings.stream()
                .map(casting -> {
                    String imageUrl = null;
                    String keyName = null;

                    if (casting.getCastingImageRequestDTO() != null) {
                        imageUrl = casting.getCastingImageRequestDTO().getImageUrl();
                        keyName = casting.getCastingImageRequestDTO().getKeyName();
                    }

                    return AmateurCasting.builder()
                            .amateurShow(amateurShow)
                            .actorName(casting.getActorName())
                            .castingName(casting.getCastingName())
                            .castingImageUrl(imageUrl)
                            .castingImageKeyName(keyName)
                            .build();
                })
                .toList();
    }

    public static AmateurNotice toAmateurNoticeEntity(AmateurEnrollRequestDTO.Notice notice, AmateurShow amateurShow) {
        if (notice == null || notice.getContent() == null) {
            return null;
        }

        String imageUrl = null;
        if (notice.getNoticeImageRequestDTO() != null) {
            imageUrl = notice.getNoticeImageRequestDTO().getImageUrl();
        }

        return AmateurNotice.builder()
                .amateurShow(amateurShow)
                .content(notice.getContent())
                .noticeImageUrl(imageUrl)
                .timeInfo(notice.getTimeInfo())
                .build();
    }

    // 이건 수정용!!
    public static AmateurNotice toAmateurNoticeEntity(AmateurUpdateRequestDTO.UpdateNotice notice, AmateurShow amateurShow) {
        if (notice == null || notice.getContent() == null) {
            return null;
        }

        String imageUrl = null;
        if (notice.getNoticeImageRequestDTO() != null) {
            imageUrl = notice.getNoticeImageRequestDTO().getImageUrl();
        }

        return AmateurNotice.builder()
                .amateurShow(amateurShow)
                .content(notice.getContent())
                .noticeImageUrl(imageUrl)
                .timeInfo(notice.getTimeInfo())
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

    // -- 소극장 공연 업데이트 --
    public static AmateurCasting toSingleCasting(AmateurUpdateRequestDTO.UpdateCasting dto, AmateurShow show) {
        return AmateurCasting.builder()
                .amateurShow(show)
                .actorName(dto.getActorName())
                .castingName(dto.getCastingName())
                .castingImageUrl(dto.getCastingImageRequestDTO().getImageUrl() != null ?
                        dto.getCastingImageRequestDTO().getImageUrl() : null)
                .build();
    }

    public static AmateurStaff toSingleStaff(AmateurUpdateRequestDTO.UpdateStaff dto, AmateurShow show) {
        return AmateurStaff.builder()
                .amateurShow(show)
                .position(dto.getPosition())
                .name(dto.getStaffName())
                .build();
    }

    public static AmateurRounds toSingleRound(AmateurUpdateRequestDTO.UpdateRounds dto, AmateurShow show) {
        return AmateurRounds.builder()
                .amateurShow(show)
                .roundNumber(dto.getRoundNumber())
                .performanceDateTime(dto.getPerformanceDateTime())
                .totalTicket(dto.getTotalTicket())
                .build();
    }

    public static AmateurTicket toSingleTicket(AmateurUpdateRequestDTO.UpdateTickets dto, AmateurShow show) {
        return AmateurTicket.builder()
                .amateurShow(show)
                .discountName(
                        (dto.getDiscountName() == null || dto.getDiscountName().isBlank())
                                ? "COMMON" : dto.getDiscountName())
                .price(dto.getPrice())
                .build();
    }


    // --소극장 공연 단건 조회 response--
    public static AmateurShowResponseDTO.AmateurShowResult toResponseDTO(AmateurShow amateurShow) {
        AmateurNotice amateurNotice = amateurShow.getAmateurNotice();
        AmateurShowResponseDTO.AmateurShowResult.Notice notice = null;
        if (amateurNotice != null) {
            notice = AmateurShowResponseDTO.AmateurShowResult.Notice.builder()
                    .noticeId(amateurNotice.getId())
                    .content(amateurNotice.getContent())
                    .noticeImageUrl(amateurNotice.getNoticeImageUrl())
                    .timeInfo(amateurNotice.getTimeInfo())
                    .build();
        }

        List<AmateurShowResponseDTO.AmateurShowResult.Tickets> tickets = Optional.ofNullable(amateurShow.getAmateurTicketList())
                .orElse(Collections.emptyList())
                .stream()
                .map(t -> AmateurShowResponseDTO.AmateurShowResult.Tickets.builder()
                        .ticketId(t.getId())
                        .discountName(t.getDiscountName())
                        .price(t.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.Casting> castings = Optional.ofNullable(amateurShow.getAmateurCastingList())
                .orElse(Collections.emptyList())
                .stream()
                .map(c -> AmateurShowResponseDTO.AmateurShowResult.Casting.builder()
                        .castingId(c.getId())
                        .actorName(c.getActorName())
                        .castingName(c.getCastingName())
                        .castingImageUrl(c.getCastingImageUrl())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.Staff> staff = Optional.ofNullable(amateurShow.getAmateurStaffList())
                .orElse(Collections.emptyList())
                .stream()
                .map(s -> AmateurShowResponseDTO.AmateurShowResult.Staff.builder()
                        .staffId(s.getId())
                        .position(s.getPosition())
                        .staffName(s.getName())
                        .build())
                .collect(Collectors.toList());

        List<AmateurShowResponseDTO.AmateurShowResult.Rounds> rounds = Optional.ofNullable(amateurShow.getAmateurRounds())
                .orElse(Collections.emptyList())
                .stream()
                .map(r -> AmateurShowResponseDTO.AmateurShowResult.Rounds.builder()
                        .roundId(r.getId())
                        .roundNumber(r.getRoundNumber())
                        .performanceDateTime(r.getPerformanceDateTime())
                        .totalTicket(r.getTotalTicket())
                        .build())
                .collect(Collectors.toList());

        String schedule = mergeSchedule(amateurShow.getStart(), amateurShow.getEnd());

        return AmateurShowResponseDTO.AmateurShowResult.builder()
                .memberId(amateurShow.getMember().getId())
                .amateurShowId(amateurShow.getId())
                .name(amateurShow.getName())
                .performerName(amateurShow.getPerformerName())
                .detailAddress(amateurShow.getDetailAddress())
                .roadAddress(amateurShow.getRoadAddress())
                .hallName(amateurShow.getHallName())
                .bankName(amateurShow.getBankName())
                .depositor(amateurShow.getDepositor())
                //.place(amateurShow.getPlace())
                .posterImageUrl(amateurShow.getPosterImageUrl())
                .schedule(schedule)
                .runtime(amateurShow.getRuntime())
                .account(amateurShow.getAccount())
                .contact(amateurShow.getContact())
                .hashtag(amateurShow.getHashtag())
                .summary(amateurShow.getSummary())
                .tickets(tickets)
                .notice(notice)
                .casting(castings)
                .staff(staff)
                .rounds(rounds)
                .build();
    }

    // start와 end 합쳐서 response용 schedule 만들기 - 컨버터 클래스의 인스턴스와 의존성 없으므로 static 설정
    public static String mergeSchedule(LocalDate start, LocalDate end) {
        try {
            if (start == null || end == null) {
                return "";
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

            // 요일 한글 매핑
            String[] days = {"월", "화", "수", "목", "금", "토", "일"};

            String startStr = start.format(formatter)
                    + " (" + days[start.getDayOfWeek().getValue() - 1] + ")";
            String endStr   = end.format(formatter)
                    + " (" + days[end.getDayOfWeek().getValue() - 1] + ")";

            // 최종 결과: "2025.10.02 (목) ~ 2025.10.05 (일)"
            return startStr + " ~ " + endStr;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
