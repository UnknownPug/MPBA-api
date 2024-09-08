package api.mpba.rastvdmy.service.component;

import api.mpba.rastvdmy.exception.ApplicationException;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class UserDataValidator {

    public static void isInvalidPassword(String password) {
        if (password == null || password.trim().isEmpty() ||
                !password.matches("^(?=.*[A-Z])(?=.*[0-9\\W]).{8,20}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid password. Password must be between 8 and 20 characters long, contain at least one" +
                            " uppercase letter, and include at least one number or special character.");
        }
    }

    public static void isInvalidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() ||
                !phoneNumber.matches("^\\+\\d{1,3}\\d{9,15}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "An invalid phone number has been entered. The number must starts with ‘+’ " +
                            "and contain (1-3 digits) and main number (9-15 digits)");
        }
    }

    public static void isInvalidDateOfBirth(@NotBlank(message = "Date of birth is mandatory") String dateOfBirth) {

        if (dateOfBirth == null || dateOfBirth.trim().isEmpty() ||
                !dateOfBirth.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid format of date of birth. Provide the date of birth in the format ‘YYYY-MM-DD’");
        }

        // Parse the date
        LocalDate dob;
        try {
            LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "The day of the month is out of range. Ensure the day is correct for the given month and year."
            );
        }
        dob = LocalDate.parse(dateOfBirth);

        // Validate the year
        int currentYear = LocalDate.now().getYear();
        if (dob.getYear() > currentYear) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid year. Year cannot be in the future."
            );
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
