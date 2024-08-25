package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible for the configuration of the application.
 * It initializes the admin and provides a RestTemplate bean for making HTTP requests.
 */
@Configuration
public class AppConfig {

    /**
     * This method provides a RestTemplate bean.
     * RestTemplate is a synchronous HTTP client that we can use to consume HTTP web services.
     *
     * @return A new instance of RestTemplate.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
