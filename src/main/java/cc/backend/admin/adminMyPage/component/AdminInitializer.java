//package cc.backend.admin.adminMyPage.component;
//
//import cc.backend.member.entity.Member;
//import cc.backend.member.repository.MemberRepository;
//import lombok.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class AdminInitializer implements ApplicationRunner {
//
//    private final MemberRepository memberRepository;
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        if (memberRepository.count() == 0) {
//            log.info("관리자 계정이 없기 때문에, 관리자를 하나 생성함");
//            Member initialAdmin = Member.builder()
//                    .username("CC_admin")
//                    .password(bCryptPasswordEncoder.encode("ccadmin0520!"))
//                    .build();
//
//            memberRepository.save(initialAdmin);
//            log.info("관리자 계정 생성 완, id : {}", initialAdmin.getId());
//
//        }
//    }
//}
