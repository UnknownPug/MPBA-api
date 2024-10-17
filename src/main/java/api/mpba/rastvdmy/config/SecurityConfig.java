package api.mpba.rastvdmy.config;

import api.mpba.rastvdmy.service.UserProfileService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Security configuration class for the application that extends Spring Security's capabilities.
 * This class defines the security filter chain, authentication provider, and various security settings
 * for the application.
 * <p>
 * The configuration includes JWT authentication, CSRF protection disabling, session management,
 * and method-level security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserProfileService userProfileService;
    private final AuthenticationEntryPoint authEntryPoint;

    /**
     * Constructor for {@code SecurityConfig} that initializes the security components.
     *
     * @param jwtAuthFilter      The {@link JwtAuthenticationFilter} for processing JWT authentication.
     * @param userProfileService The {@link UserProfileService} for loading user details.
     * @param authEntryPoint     The {@link AuthenticationEntryPoint} to handle authentication exceptions.
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          @Lazy UserProfileService userProfileService,
                          @Qualifier("authenticationEntryPointImpl") AuthenticationEntryPoint authEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userProfileService = userProfileService;
        this.authEntryPoint = authEntryPoint;
    }

    /**
     * Configures the security filter chain for the application.
     * This method sets up CSRF protection, XSS protection, session management,
     * request authorization, and exception handling.
     *
     * @param http The {@link HttpSecurity} object used to configure the security settings.
     * @return The configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during the configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers ->
                        headers.xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        ).contentSecurityPolicy(
                                cps -> cps.policyDirectives("script-src 'self'")
                        ))
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/v1/auth/**")
                                .permitAll().anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authEntryPoint));
        return http.build();
    }

    /**
     * Configures the authentication provider for the application.
     * This method uses a {@link DaoAuthenticationProvider} to authenticate users
     * based on user details provided by the {@link UserProfileService} and
     * encodes passwords using the {@link PasswordEncoder}.
     *
     * @return The configured {@link AuthenticationProvider}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userProfileService.userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provides the authentication manager for the application.
     * This bean is required for managing authentication operations.
     *
     * @param config The {@link AuthenticationConfiguration} object used to retrieve the authentication manager.
     * @return The configured {@link AuthenticationManager}.
     * @throws Exception if an error occurs while retrieving the authentication manager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the password encoder to use for encoding passwords.
     * This method returns a {@link BCryptPasswordEncoder} instance for secure password hashing.
     *
     * @return The configured {@link PasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}