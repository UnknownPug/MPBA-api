package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.handler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This class is responsible for handling errors in the application.
 * It implements the ErrorController interface and overrides its method to perform specific actions.
 * The whitelabelError method is used to handle different HTTP status codes and return the corresponding error page.
 */
@Controller
public class ErrorHandler implements ErrorController {
    /**
     * This method is used to handle different HTTP status codes.
     * It retrieves the status code from the request attributes and returns the corresponding error page.
     *
     * @param request The HTTP request.
     * @return The name of the error page.
     */
    @RequestMapping("/error")
    public String whitelabelError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "error-401";
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "error-403";
            }
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error-404";
            }
            if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error-500";
            }
        }
        return "error-400";
    }
}
