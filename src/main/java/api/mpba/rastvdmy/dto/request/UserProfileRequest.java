package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * This class represents a request to create or update a user's profile in the system.
 *
 * @param id              The unique identifier for the user.
 * @param name            The user's first name.
 * @param surname         The user's surname.
 * @param dateOfBirth     The user's date of birth, formatted as a string.
 * @param countryOfOrigin The user's country of origin.
 * @param email           The user's email address.
 * @param password        The user's password.
 * @param phoneNumber     The user's phone number.
 * @param avatar          The URL or path to the user's avatar image.
 * @param status          The current status of the user (active, inactive, etc.).
 * @param role            The user's assigned role within the system.
 */
public record UserProfileRequest(
        UUID id,

        String name,

        String surname,

        @JsonProperty("date_of_birth")
        String dateOfBirth,

        @JsonProperty("country_of_origin")
        String countryOfOrigin,

        String email,

        String password,

        @JsonProperty("phone_number")
        String phoneNumber,

        String avatar,

        UserStatus status,

        UserRole role
) {}
