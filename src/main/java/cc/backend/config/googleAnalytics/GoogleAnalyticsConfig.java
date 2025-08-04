package cc.backend.config.googleAnalytics;

import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class GoogleAnalyticsConfig {

    @Bean
    public BetaAnalyticsDataClient betaAnalyticsDataClient() throws IOException {
        String base64Key = System.getenv("GA_SERVICE_ACCOUNT_KEY_BASE64");

        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64Key)));

        return BetaAnalyticsDataClient.create(
                BetaAnalyticsDataSettings.newBuilder()
                        .setCredentialsProvider(() -> credentials)
                        .build()
        );
    }
}
