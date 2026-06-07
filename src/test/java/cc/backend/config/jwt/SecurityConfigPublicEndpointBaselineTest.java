package cc.backend.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SecurityConfigPublicEndpointBaselineTest {

    private static final Path SECURITY_CONFIG = Path.of(
            "src/main/java/cc/backend/config/jwt/SecurityConfig.java"
    );

    @Test
    void publicAndPrivateEndpointBoundariesRemainExplicit() throws IOException {
        String source = Files.readString(SECURITY_CONFIG);

        assertThat(source).contains(".requestMatchers(\"/actuator/health\").permitAll()");
        assertThat(source).contains(".requestMatchers(\"/auth/**\").permitAll()");
        assertThat(source).contains(".requestMatchers(HttpMethod.GET, \"/kakaoPay/approve\", \"/kakaoPay/cancel\", \"/kakaoPay/fail\").permitAll()");
        assertThat(source).contains(".requestMatchers(HttpMethod.POST, \"/kakaoPay/ready\").authenticated()");
        assertThat(source).contains(".requestMatchers(\"/swagger-ui/**\").permitAll()");
        assertThat(source).contains(".requestMatchers(\"/admin/login\").permitAll()");
        assertThat(source).contains(".requestMatchers(HttpMethod.GET, \"/amateurs/ranking\").permitAll()");
        assertThat(source).contains(".requestMatchers(\"/upload/**\").permitAll()");
        assertThat(source).contains(".anyRequest().authenticated()");
    }
}
