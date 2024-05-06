package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.AdminInitializer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible for the configuration of the application.
 * It initializes the admin and provides a RestTemplate bean for making HTTP requests.
 */
@Configuration
public class AppConfig {

    private final AdminInitializer adminInitializer;

    /**
     * Constructor for AppConfig.
     * It takes an AdminInitializer as a parameter which is used to initialize the admin.
     *
     * @param adminInitializer The admin initializer.
     */
    @Autowired
    public AppConfig(AdminInitializer adminInitializer) {
        this.adminInitializer = adminInitializer;
    }

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

    /**
     * This method is called after the AppConfig bean is created and its properties are set.
     * It calls the initializeAdmin method of the AdminInitializer to initialize the admin.
     */
    @PostConstruct
    public void init() {
        adminInitializer.initializeAdmin();
    }
}
