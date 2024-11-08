package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

import static api.mpba.rastvdmy.service.validator.ValidationUtils.*;

/**
 * Validates the surname of a user.
 * The surname must start with an uppercase letter followed by 2-19 lowercase letters.
 */
public class SurnameValidation implements ValidationStrategy {
    private static final String SURNAME_REGEX = "^[A-Z][a-z]{2,19}$";

    /**
     * Validates the given surname.
     *
     * @param surname the surname to validate
     * @throws ApplicationException if the surname is invalid
     */
    @Override
    public void validate(String surname) throws ApplicationException {
        validateNotEmpty(surname, "Surname cannot be null or empty.");
        if (!surname.matches(SURNAME_REGEX)) {
            throwApplicationException("Invalid surname. Surname must start with an uppercase letter, " +
                    "followed by 2-19 lowercase letters.");
        }
    }
}