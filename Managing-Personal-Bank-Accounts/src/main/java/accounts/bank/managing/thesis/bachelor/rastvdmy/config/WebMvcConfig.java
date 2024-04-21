package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This class is responsible for the configuration of the web MVC.
 * It implements the WebMvcConfigurer interface and overrides its methods to perform specific actions.
 * The addInterceptors method is used to register the LoggingInterceptor and AuthenticationInterceptor.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * This method is used to add interceptors to the InterceptorRegistry.
     * It registers the LoggingInterceptor with order 1 and the AuthenticationInterceptor with order 2.
     * The order determines the execution order of the interceptors, with lower values having higher priority.
     *
     * @param registry The InterceptorRegistry instance.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoggingInterceptor()).order(1);
        registry.addInterceptor(new AuthenticationInterceptor()).order(2);
    }
}