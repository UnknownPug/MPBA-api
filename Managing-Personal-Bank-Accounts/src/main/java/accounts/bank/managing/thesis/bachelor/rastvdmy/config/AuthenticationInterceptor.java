package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class is an interceptor for handling authentication-related tasks.
 * It implements the HandlerInterceptor interface and overrides its methods to perform specific actions.
 * The preHandle method is used to log the authentication status of the user.
 * The postHandle method is used to log the request id after authentication is done.
 * The afterCompletion method is currently not used for additional processing.
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    /**
     * This method is called before the actual handler is executed.
     * It logs the authentication status of the user.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     * @param handler  The handler object.
     * @return true to continue the request processing, false to interrupt it.
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null) {
                String username = authentication.getName();
                String event = authentication.isAuthenticated() ? "Logged In" : "Logged Out";
                log.info("User {} - {}", username, event);
            }
            return true; // Continue with the request processing
        } catch (Exception e) {
            log.error("An error occurred:", e);
            throw e;
        }
    }

    /**
     * This method is called after the handler is executed.
     * It logs the request id after authentication is done.
     *
     * @param request      The HTTP request.
     * @param response     The HTTP response.
     * @param handler      The handler object.
     * @param modelAndView The model and view object.
     */
    @Override
    public void postHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            ModelAndView modelAndView) {
        try {
            String requestId = (String) request.getAttribute("request-id");
            log.info("Request id {} - Authentication Done", requestId);
        } catch (Exception e) {
            log.error("An error occurred:", e);
            throw e;
        }
    }

    /**
     * This method is called after the complete request has finished.
     * It's not used for additional processing in this class.
     *
     * @param request   The HTTP request.
     * @param response  The HTTP response.
     * @param handler   The handler object.
     * @param exception Any exception thrown during the execution of the handler.
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception exception) throws Exception {
        // No additional processing needed here
    }
}