//package cc.backend.admin.adminMyPage.component;
//
//import cc.backend.admin.adminMyPage.entity.Admin;
//import cc.backend.admin.adminMyPage.repository.AdminRepository;
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
//    private final AdminRepository adminRepository;
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        if (adminRepository.count() == 0) {
//            log.info("관리자 계정이 없기 때문에, 관리자를 하나 생성함");
//            Admin initialAdmin = Admin.builder()
//                    .adminId("CC_admin")
//                    .password(bCryptPasswordEncoder.encode("ccadmin0520!"))
//                    .accountNumber("123-456-7890")
//                    .build();
//
//            adminRepository.save(initialAdmin);
//            log.info("관리자 계정 생성 완, id : {}", initialAdmin.getAdminId());
//
//        }
//    }
//}
