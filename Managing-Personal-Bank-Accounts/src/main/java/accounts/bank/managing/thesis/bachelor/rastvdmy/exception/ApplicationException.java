package accounts.bank.managing.thesis.bachelor.rastvdmy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {
    @Getter
    private final HttpStatus httpStatus;
    private final String message;

    public ApplicationException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
