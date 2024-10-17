package api.mpba.rastvdmy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Custom implementation of {@link AuthenticationEntryPoint} used to handle authentication exceptions.
 * This class serves as an entry point for unauthenticated requests and utilizes a {@link HandlerExceptionResolver}
 * to resolve the authentication exception.
 * <p>
 * It is primarily used in security configurations to handle cases where a user tries to access a resource
 * without proper authentication, by resolving the exception and responding accordingly.
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    private final HandlerExceptionResolver resolver;

    /**
     * Constructor for {@code AuthenticationEntryPointImpl} that injects the {@link HandlerExceptionResolver}
     * to handle the resolution of exceptions related to authentication.
     *
     * @param resolver The {@link HandlerExceptionResolver} used for handling exceptions.
     */
    public AuthenticationEntryPointImpl(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Handles the commencement of an authentication error response.
     * This method is called whenever an unauthenticated user requests a secured HTTP resource,
     * and an {@link AuthenticationException} is thrown. The method delegates the exception
     * resolution to the injected {@link HandlerExceptionResolver}.
     *
     * @param request       The HTTP request that resulted in an authentication exception.
     * @param response      The HTTP response to be sent.
     * @param authException The exception that was thrown when authentication failed.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null, authException);
    }
}