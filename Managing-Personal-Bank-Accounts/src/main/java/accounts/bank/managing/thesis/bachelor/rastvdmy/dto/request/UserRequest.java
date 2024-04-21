package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * This class represents a request for a user.
 * It contains the name, surname, date of birth, country of origin,
 * email, password, phone number, and user role of the user.
 */
public record UserRequest(
        String name,

        String surname,

        @JsonProperty("date_of_birth")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dateOfBirth,

        @JsonProperty("country_of_origin")
        String countryOfOrigin,

        String email,

        String password,

        @JsonProperty("phone_number")
        String phoneNumber,

        @JsonProperty("user_role")
        String userRole) {

}
