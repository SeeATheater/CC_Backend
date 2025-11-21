package cc.backend.admin.adminMyPage.component;

import org.springframework.beans.factory.annotation.Value;
import cc.backend.member.entity.Member;
import cc.backend.member.enumerate.Role;
import cc.backend.member.repository.MemberRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Value("${admin.password}")
    private String defaultAdminPassword;


    @Override
    public void run(ApplicationArguments args) throws Exception {


        boolean hasAdmin = memberRepository.existsByRole(Role.ADMIN);
        if (!hasAdmin) {
            log.info("관리자 계정이 없기 때문에, 임시 관리자를 하나 생성함");
            Member initialAdmin = Member.builder()
                    .username("CC_admin")
                    .email("seeatheateradmin@gmail.com")
                    .name("임시 관리자")
                    .role(Role.ADMIN)
                    .password(bCryptPasswordEncoder.encode(defaultAdminPassword))
                    .build();

            memberRepository.save(initialAdmin);
            log.info("임시 관리자 계정 생성 완, id : {}", initialAdmin.getId());

        }
    }
}
