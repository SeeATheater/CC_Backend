//package cc.backend.scheduler;
//
//import cc.backend.amateurShow.entity.AmateurShow;
//import cc.backend.amateurShow.entity.AmateurShowStatus;
//import cc.backend.amateurShow.repository.AmateurShowRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class ShowStatusScheduler {
//
//    private final AmateurShowRepository amateurShowRepository;
//
//    @Transactional
//    @Scheduled(cron = "5 0 0 * * *")
//    public void updateShowStatus() {
//        LocalDate today = LocalDate.now();
//        List<AmateurShowStatus> targetStatus = List.of(AmateurShowStatus.APPROVED_YET, AmateurShowStatus.APPROVED_ONGOING);
//        List<AmateurShow> candidateShows = amateurShowRepository.findByStatusIn(targetStatus);
//
//        List<Long> idsToBeOngoing = new ArrayList<>();
//        List<Long> idsToBeEnded = new ArrayList<>();
//
//        for(AmateurShow show: candidateShows){
//            parseSchedule(show.getSchedule()).ifPresent(dates ->{
//                LocalDate startDate = dates[0];
//                LocalDate endDate = dates[1];
//
//                if(show.getStatus() == AmateurShowStatus.APPROVED_YET && today.isAfter(startDate) && today.isBefore(endDate)){
//                    idsToBeOngoing.add(show.getId());
//                    log.info("공연 아이디 {} (공연전->공연중) 바뀌기 전 id 리스트에 추가", show.getId());
//                }
//                else if(show.getStatus() == AmateurShowStatus.APPROVED_ONGOING && today.isAfter(endDate)){
//                    idsToBeEnded.add(show.getId());
//                    log.info("공연 아이디 {} (공연중->공연종료) 바뀌기 전에 id 리스트에 추가", show.getId());
//
//                }
//            });
//        }
//
//        if(!idsToBeOngoing.isEmpty()){
//            amateurShowRepository.updateStatusByIds(idsToBeOngoing, AmateurShowStatus.APPROVED_ONGOING);
//            log.info("{}개의 공연이 (공연전->공연중) 상태로 변경", idsToBeEnded.size());
//
//        }
//
//        if (!idsToBeEnded.isEmpty()) {
//            amateurShowRepository.updateStatusByIds(idsToBeEnded, AmateurShowStatus.APPROVED_ENDED);
//            log.info("{}개의 공연이 (공연중->공연종료) 상태로 변경", idsToBeEnded.size());
//        }
//
//
//    }
//
//    // 윤호 코드 재사용 -> start, end 로 분리 예정
//    private Optional<LocalDate[]> parseSchedule(String schedule) {
//        try {
//            if (schedule == null || !schedule.contains("~")) return Optional.empty();
//
//            String[] parts = schedule.split("~"); // ~ 기준으로 자르고
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
//
//            LocalDate start = LocalDate.parse(parts[0].trim(), formatter); // 시작일
//            LocalDate end = LocalDate.parse(parts[1].trim(), formatter); // 종료일
//
//            return Optional.of(new LocalDate[]{start, end});
//        } catch (Exception e) {
//            return Optional.empty();
//        }
//    }
//}
//
//
