package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        try {
            // Log incoming request information
            String requestURI = request.getRequestURI();
            String requestId = UUID.randomUUID().toString(); // Generate a unique request ID
            String method = request.getMethod();

            request.setAttribute("start", System.currentTimeMillis());
            request.setAttribute("request-id", requestId);
            request.setAttribute("request-uri", requestURI);

            log.info("Request id {} - Calling {} {}", requestId, method, requestURI);
            return true;
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
            // Log response information
            int responseStatus = response.getStatus();
            log.info("Response sent with status {}", responseStatus);
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
            Exception exception) {
        try {
            String requestId = (String) request.getAttribute("request-id");
            Long startTime = (Long) request.getAttribute("start");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;

                log.info("Request id {} - Status {} - Completed in {}ms",
                        requestId,
                        response.getStatus(),
                        duration);
            } else {
                log.warn("Request id {} - Status {} - Unable to calculate duration. Start time not found.",
                        requestId,
                        response.getStatus());
            }

            // Clean up request attributes
            request.removeAttribute("start");
            request.removeAttribute("request-id");
            request.removeAttribute("request-uri");
        } catch (Exception e) {
            log.error("An error occurred:", e);
            throw e;
        }
    }
}