package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

/**
 * This class represents a user in the banking system.
 *
 * @param name            The name of the user.
 * @param surname         The surname of the user.
 * @param dateOfBirth     The date of birth of the user.
 * @param countryOfOrigin The country of origin of the user.
 * @param email           The email of the user.
 * @param password        The password of the user.
 * @param phoneNumber     The phone number of the user.
 */
public record UserRequest(

        @NotBlank(message = "Name is mandatory")
        String name,

        @NotBlank(message = "Surname is mandatory")
        String surname,

        @NotBlank(message = "Date of birth is mandatory")
        @JsonProperty("date_of_birth")
        String dateOfBirth,

        @NotBlank(message = "Country of origin is mandatory")
        @JsonProperty("country_of_origin")
        String countryOfOrigin,

        @NotBlank(message = "Email is mandatory")
        String email,

        @NotBlank(message = "Password is mandatory")
        String password,

        @NotBlank(message = "Phone number is mandatory")
        @JsonProperty("phone_number")
        String phoneNumber
) {}
