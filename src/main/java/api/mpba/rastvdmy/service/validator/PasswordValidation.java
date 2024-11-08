package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

import static api.mpba.rastvdmy.service.validator.ValidationUtils.*;

/**
 * Validates the password according to the specified rules.
 * The password must contain at least one uppercase letter,
 * one digit or special character, and be 8-20 characters long.
 */
public class PasswordValidation implements ValidationStrategy {
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[0-9\\W]).{8,20}$";

    /**
     * Validates the given password.
     *
     * @param password the password to validate
     * @throws ApplicationException if the password is invalid
     */
    @Override
    public void validate(String password) throws ApplicationException {
        password = password.trim();
        validateNotEmpty(password, "Password cannot be null or empty.");
        if (!password.matches(PASSWORD_REGEX)) {
            throwApplicationException("Invalid password. Password must contain at least " +
                    "one uppercase letter, one digit or special character, and be 8-20 characters long.");
        }
    }
}
