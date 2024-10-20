package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

/**
 * A utility class for validating user data such as name, surname, email,
 * password, phone number, and date of birth.
 */
public class UserDataValidator {

    /**
     * Validates the given name.
     *
     * @param name the name to validate
     * @throws ApplicationException if the name is null, empty, or does not match the required pattern
     */
    public static void isInvalidName(String name) {
        if (name == null || name.isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Name cannot be null or empty.");
        }

        if (!name.matches("^[A-Z][a-z]{2,19}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid name. Name must start with an uppercase letter," +
                            " followed by 2-19 lowercase letters."
            );
        }
    }

    /**
     * Validates the given surname.
     *
     * @param surname the surname to validate
     * @throws ApplicationException if the surname is null, empty, or does not match the required pattern
     */
    public static void isInvalidSurname(String surname) {
        if (surname == null || surname.isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Surname cannot be null or empty.");
        }

        if (!surname.matches("^[A-Z][a-z]{2,19}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid Surname. Surname must start with an uppercase letter," +
                            " followed by 2-19 lowercase letters."
            );
        }
    }

    /**
     * Validates the given email address.
     *
     * @param email the email address to validate
     * @throws ApplicationException if the email is null, empty, or does not match the required pattern
     */
    public static void isInvalidEmail(String email) {
        if (email == null || email.isBlank() ||
                !email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid email. Provide a valid email address.");
        }
    }

    /**
     * Validates the given password.
     *
     * @param password the password to validate
     * @throws ApplicationException if the password is null, empty, or does not meet the required criteria
     */
    public static void isInvalidPassword(String password) {
        if (password == null || password.isBlank() ||
                !password.matches("^(?=.*[A-Z])(?=.*[0-9\\W]).{8,20}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid password. Password must be between 8 and 20 characters long, contain at least one" +
                            " uppercase letter, and include at least one number or special character."
            );
        }
    }

    /**
     * Validates the given phone number.
     *
     * @param phoneNumber the phone number to validate
     * @throws ApplicationException if the phone number is null, empty, or does not match the required format
     */
    public static void isInvalidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Phone number cannot be null or empty.");
        }
        String sanitizedPhoneNumber = phoneNumber.replaceAll("\\s", "");

        if (!sanitizedPhoneNumber.matches("^\\+\\d{1,3}\\d{4,14}$") || sanitizedPhoneNumber.length() > 16) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid phone number. It must start with ‘+’," +
                            " followed by 1-3 digits (country code) and 4-14 digits (subscriber number)," +
                            " with a total length of up to 16."
            );
        }
    }

    /**
     * Validates the given date of birth.
     *
     * @param dateOfBirth the date of birth to validate
     * @throws ApplicationException if the date of birth is null, empty, or does not match the required format
     */
    public static void isInvalidDateOfBirth(String dateOfBirth) {

        if (dateOfBirth == null || dateOfBirth.isBlank() ||
                !dateOfBirth.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid format of date of birth. Provide the date of birth in the format ‘YYYY-MM-DD’"
            );
        }

        // Parse the date
        LocalDate dob;
        try {
            LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "The day of the month is out of range. Ensure the day is correct for the given month and year.");
        }
        dob = LocalDate.parse(dateOfBirth);

        // Validate the year
        int currentYear = LocalDate.now().getYear();
        if (dob.getYear() > currentYear) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid year. Year cannot be in the future.");
        }

        // Validate the month and day
        if (dob.getDayOfMonth() > dob.lengthOfMonth()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid date. Ensure the day is correct for the given month and year."
            );
        }

        // Calculate the user's age
        int age = Period.between(dob, LocalDate.now()).getYears();

        // Validate the age range
        if (age < 18 || age > 100) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid date of birth. Ensure entered age between 18 and 100 years old."
            );
        }
    }
}
