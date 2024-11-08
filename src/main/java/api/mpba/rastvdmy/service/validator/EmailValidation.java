package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

import static api.mpba.rastvdmy.service.validator.ValidationUtils.*;

/**
 * A strategy for validating email addresses.
 */
public class EmailValidation implements ValidationStrategy {
    private static final String EMAIL_REGEX = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    /**
     * Validates the given email.
     *
     * @param email the email to validate
     * @throws ApplicationException if the email is invalid
     */
    @Override
    public void validate(String email) throws ApplicationException {
        validateNotEmpty(email, "Email cannot be null or empty.");
        if (!email.matches(EMAIL_REGEX)) {
            throwApplicationException("Invalid email. Provide a valid email address.");
        }
    }
}
