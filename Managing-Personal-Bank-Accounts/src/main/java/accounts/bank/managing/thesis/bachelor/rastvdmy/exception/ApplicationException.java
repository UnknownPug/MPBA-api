package accounts.bank.managing.thesis.bachelor.rastvdmy.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * This class represents a custom exception in the application.
 * It extends the RuntimeException class and includes an HttpStatus and a message.
 * The HttpStatus represents the HTTP status code that should be returned when this exception is thrown.
 * The message provides additional information about the exception.
 */
public class ApplicationException extends RuntimeException {
    /**
     * The HTTP status code that should be returned when this exception is thrown.
     */
    @Getter
    private final HttpStatus httpStatus;

    /**
     * The message that provides additional information about the exception.
     */
    private final String message;

    /**
     * Constructor for the ApplicationException class.
     *
     * @param httpStatus The HTTP status code that should be returned when this exception is thrown.
     * @param message    The message that provides additional information about the exception.
     */
    public ApplicationException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * Returns the message of the exception.
     *
     * @return The message of the exception.
     */
    @Override
    public String getMessage() {
        return message;
    }
}
