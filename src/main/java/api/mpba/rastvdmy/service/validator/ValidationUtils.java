package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;
import org.springframework.http.HttpStatus;

/**
 * Utility class for common validation methods.
 */
public class ValidationUtils {

    /**
     * Validates that the given field is not empty or null.
     *
     * @param field   the field to validate
     * @param message the error message to throw if the field is empty or null
     * @throws ApplicationException if the field is empty or null
     */
    public static void validateNotEmpty(String field, String message) {
        if (field == null || field.isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /**
     * Throws an ApplicationException with the given message.
     *
     * @param message the error message
     * @throws ApplicationException always thrown with the given message
     */
    public static void throwApplicationException(String message) {
        throw new ApplicationException(HttpStatus.BAD_REQUEST, message);
    }
}
