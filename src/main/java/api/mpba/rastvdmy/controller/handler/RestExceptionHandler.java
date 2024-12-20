package api.mpba.rastvdmy.controller.handler;

import api.mpba.rastvdmy.dto.response.ErrorResponse;
import api.mpba.rastvdmy.exception.ApplicationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for handling exceptions in the application.
 * It extends the ResponseEntityExceptionHandler class and overrides its methods to perform specific actions.
 * The wrongFormatException method is used to handle ConstraintViolationException.
 * The applicationException method is used to handle ApplicationException.
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    public static final String APPLICATION_FIELD = "APPLICATION_ERROR";

    /**
     * This method is used to handle ConstraintViolationException.
     * It creates a list of ErrorResponse instances for each constraint violation and returns it to the response entity.
     *
     * @param ex The ConstraintViolationException instance.
     * @return The ResponseEntity instance containing the list of ErrorResponse instances and the HTTP status.
     */
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<List<ErrorResponse>> wrongFormatException(ConstraintViolationException ex) {
        List<ErrorResponse> responses = new ArrayList<>();
        ex.getConstraintViolations().forEach(error ->
                responses.add(
                        new ErrorResponse(
                                error.getMessage(),
                                error.getInvalidValue().toString()
                        )
                )
        );
        return new ResponseEntity<>(responses, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * This method is used to handle ApplicationException.
     * It creates an error message and returns it to the response entity.
     *
     * @param ex The ApplicationException instance.
     * @return The ResponseEntity instance containing the error message and the HTTP status.
     */
    @ExceptionHandler(value = {ApplicationException.class})
    protected ResponseEntity<String> applicationException(ApplicationException ex) {
        return httpResponse(APPLICATION_FIELD + ": " + ex.getMessage(), ex.getHttpStatus());
    }

    /**
     * This method is used to create a response entity with the given message and HTTP status.
     *
     * @param msg        The error message.
     * @param httpStatus The HTTP status.
     * @return The ResponseEntity instance containing the error message and the HTTP status.
     */
    private ResponseEntity<String> httpResponse(String msg, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body(msg);
    }
}
