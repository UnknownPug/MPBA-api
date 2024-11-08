package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

import static api.mpba.rastvdmy.service.validator.ValidationUtils.*;

/**
 * Validates a phone number based on a specific format.
 */
public class PhoneNumValidation implements ValidationStrategy {
    private static final String PHONE_REGEX = "^\\+\\d{1,3}\\d{4,14}$";

    /**
     * Validates the given phone number.
     *
     * @param phoneNum the phone number to validate
     * @throws ApplicationException if the phone number is invalid
     */
    @Override
    public void validate(String phoneNum) throws ApplicationException {
        validateNotEmpty(phoneNum, "Phone number cannot be null or empty.");
        String sanitizedPhoneNumber = phoneNum.replaceAll("\\s", "");
        if (!sanitizedPhoneNumber.matches(PHONE_REGEX)) {
            throwApplicationException("Invalid phone number. It must start with ‘+’, " +
                    "followed by 1-3 digits (country code) and 4-14 digits (subscriber number), " +
                    "with a total length of up to 16.");
        }
    }
}