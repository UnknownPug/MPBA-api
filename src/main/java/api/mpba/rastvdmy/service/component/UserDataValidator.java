package api.mpba.rastvdmy.service.component;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.Period;

public class UserDataValidator {

    public static boolean isValidPassword(String password) {
        // Password validation logic
        return password != null && password.matches("^(?=.*[A-Z])(?=.*[0-9\\W]).{8,20}$");
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Ensure the phone number is not null or blank and follows the required pattern
        return phoneNumber != null &&
                !phoneNumber.trim().isEmpty() &&
                phoneNumber.matches("^\\+\\d{1,3}\\d{9,15}$");
    }

    public static boolean isValidDateOfBirth(@NotBlank(message = "Date of birth is mandatory") String dateOfBirth) {
        // Check for null or invalid format upfront
        if (dateOfBirth == null || !dateOfBirth.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return false;
        }
        // Parse the date
        LocalDate dob = LocalDate.parse(dateOfBirth);

        // Calculate the user's age
        int age = Period.between(dob, LocalDate.now()).getYears();

        // Validate the age range (18-100)
        return age >= 18 && age <= 100;
    }
}
