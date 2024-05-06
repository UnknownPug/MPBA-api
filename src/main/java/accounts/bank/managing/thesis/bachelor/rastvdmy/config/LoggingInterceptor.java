package accounts.bank.managing.thesis.bachelor.rastvdmy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * This class is an interceptor for logging request and response information.
 * It implements the HandlerInterceptor interface and overrides its methods to perform specific actions.
 * The preHandle method is used to log the incoming request information.
 * The postHandle method is used to log the response status.
 * The afterCompletion method is used to log the request completion status and clean up request attributes.
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    /**
     * This method is called before the actual handler is executed.
     * It logs the incoming request information and sets some attributes on the request.
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

    /**
     * This method is called after the handler is executed.
     * It logs the response status.
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
            int responseStatus = response.getStatus();
            log.info("Response sent with status {}", responseStatus);
        } catch (Exception e) {
            log.error("An error occurred:", e);
            throw e;
        }
    }

    /**
     * This method is called after the complete request has finished.
     * It logs the request completion status and cleans up request attributes.
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