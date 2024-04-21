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

@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

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

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception exception) throws Exception {
        // No additional processing needed here
    }
}