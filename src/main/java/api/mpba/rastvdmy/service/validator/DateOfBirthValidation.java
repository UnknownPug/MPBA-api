package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

import static api.mpba.rastvdmy.service.validator.ValidationUtils.*;

/**
 * Validates the date of birth for a user.
 * Ensures the date of birth is not null or empty, matches the required format,
 * is a valid date, is not in the future, and the age is between 18 and 100 years.
 */
public class DateOfBirthValidation implements ValidationStrategy {
    private static final String DATE_OF_BIRTH_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final int MIN_AGE = 18;
    private static final int MAX_AGE = 100;

    /**
     * Validates the given date of birth.
     *
     * @param dateOfBirth the date of birth to validate
     * @throws ApplicationException if the date of birth is invalid
     */
    @Override
    public void validate(String dateOfBirth) throws ApplicationException {
        validateNotEmpty(dateOfBirth, "Date of birth cannot be null or empty.");
        if (!dateOfBirth.matches(DATE_OF_BIRTH_REGEX)) {
            throwApplicationException("Invalid format of date of birth. " +
                    "Provide the date of birth in the format ‘YYYY-MM-DD’");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException e) {
            throwApplicationException("The day of the month is out of range. " +
                    "Ensure the day is correct for the given month and year.");
            return; // Only for clarity, exception would terminate the flow anyway.
        }

        if (dob.isAfter(LocalDate.now())) {
            throwApplicationException("Invalid year. Year cannot be in the future.");
        }

        int age = calculateAge(dob);
        if (age < MIN_AGE || age > MAX_AGE) {
            throwApplicationException("Invalid date of birth. Ensure entered age is between 18 and 100 years old.");
        }
    }

    /**
     * Calculates the age based on the given date of birth.
     *
     * @param dob the date of birth
     * @return the calculated age
     */
    private static int calculateAge(LocalDate dob) {
        return Period.between(dob, LocalDate.now()).getYears();
    }
}