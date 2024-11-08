package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

/**
 * Strategy interface for validating user data.
 */
public interface ValidationStrategy {
    /**
     * Validates the given value.
     *
     * @param value the value to validate
     * @throws ApplicationException if the value is invalid
     */
    void validate(String value) throws ApplicationException;
}