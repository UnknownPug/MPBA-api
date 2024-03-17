package accounts.bank.managing.thesis.bachelor.rastvdmy.controller.handler;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.ErrorResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    public static final String APPLICATION_FIELD = "APPLICATION_ERROR";

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

    @ExceptionHandler(value = {ApplicationException.class})
    protected ResponseEntity<Object> applicationException(ApplicationException ex) {
        return httpResponse(APPLICATION_FIELD + ": " + ex.getMessage(), ex.getHttpStatus());
    }

    private ResponseEntity<Object> httpResponse(String msg, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body(msg);
    }
}
