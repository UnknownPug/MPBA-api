package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

import static api.mpba.rastvdmy.service.validator.ValidationUtils.*;

/**
 * A validation strategy for validating names.
 */
public class NameValidation implements ValidationStrategy {
    private static final String NAME_REGEX = "^[A-Z][a-z]{2,19}$";

    /**
     * Validates the given name.
     *
     * @param name the name to validate
     * @throws ApplicationException if the name is invalid
     */
    @Override
    public void validate(String name) throws ApplicationException {
        validateNotEmpty(name, "Name cannot be null or empty.");
        if (!name.matches(NAME_REGEX)) {
            throwApplicationException("Invalid name. Name must start with an uppercase letter, " +
                    "followed by 2-19 lowercase letters.");
        }
    }
}