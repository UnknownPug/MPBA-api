package api.mpba.rastvdmy.config;

import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.UserProfileService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Filter for JWT authentication that extends {@link OncePerRequestFilter}.
 * This filter intercepts incoming HTTP requests to validate the JWT provided in the Authorization header.
 * It extracts the user email from the JWT and sets the authentication in the security context if the token is valid.
 * <p>
 * The filter is designed to be used in conjunction with Spring Security to manage JWT-based authentication.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserProfileService userProfileService;
    private final HandlerExceptionResolver resolver;

    /**
     * Constructor for {@code JwtAuthenticationFilter} that injects the necessary services.
     *
     * @param jwtService          The {@link JwtService} used for JWT operations.
     * @param userProfileService   The {@link UserProfileService} for loading user details.
     * @param resolver            The {@link HandlerExceptionResolver} to handle exceptions.
     */
    public JwtAuthenticationFilter(JwtService jwtService,
                                   @Lazy UserProfileService userProfileService,
                                   @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.userProfileService = userProfileService;
        this.resolver = resolver;
    }

    /**
     * The main filter method that processes the incoming HTTP request and validates the JWT.
     * This method checks for the presence of the Authorization header, extracts the JWT,
     * and validates it against the user's details. If the token is valid, it sets the authentication
     * in the security context.
     *
     * @param request     The incoming HTTP request.
     * @param response    The HTTP response to be sent.
     * @param filterChain The filter chain to pass the request and response to the next filter.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract JWT from the Authorization header
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt);

            if (StringUtils.isNotEmpty(userEmail)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userProfileService.userDetailsService()
                        .loadUserByUsername(userEmail);

                // Validate the JWT and set the authentication in the SecurityContext
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}