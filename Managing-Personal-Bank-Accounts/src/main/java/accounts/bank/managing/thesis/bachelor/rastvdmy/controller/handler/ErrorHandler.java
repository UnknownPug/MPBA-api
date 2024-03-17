package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.handler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorHandler implements ErrorController {
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
