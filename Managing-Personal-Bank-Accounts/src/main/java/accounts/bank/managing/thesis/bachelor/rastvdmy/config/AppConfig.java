package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.AdminInitializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    private final AdminInitializer adminInitializer;

    @Autowired
    public AppConfig(AdminInitializer adminInitializer) {
        this.adminInitializer = adminInitializer;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @PostConstruct
    public void init() {
        adminInitializer.initializeAdmin();
    }
}
