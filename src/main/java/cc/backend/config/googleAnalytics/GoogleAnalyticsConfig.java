package cc.backend.config.googleAnalytics;

import com.google.analytics.data.v1beta.BetaAnalyticsDataClient;
import com.google.analytics.data.v1beta.BetaAnalyticsDataSettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GoogleAnalyticsConfig {

    @Value("${google.credentials.path}")
    private Resource googleCredentialsResource;

    @Bean
    public BetaAnalyticsDataClient betaAnalyticsDataClient() throws IOException {
        try (InputStream inputStream = googleCredentialsResource.getInputStream()) {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream);

            return BetaAnalyticsDataClient.create(
                    BetaAnalyticsDataSettings.newBuilder()
                            .setCredentialsProvider(() -> credentials)
                            .build()
            );
        }
    }
}
