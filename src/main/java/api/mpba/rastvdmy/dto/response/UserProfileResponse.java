package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

/**
 * This class represents a response for a user profile in the banking system.
 *
 * @param id              The unique identifier of the user.
 * @param name            The name of the user.
 * @param surname         The surname of the user.
 * @param dateOfBirth     The date of birth of the user.
 * @param countryOfOrigin The country of origin of the user.
 * @param email           The email address of the user.
 * @param password        The password of the user.
 * @param phoneNumber     The phone number of the user.
 * @param avatar          The avatar of the user.
 * @param status          The status of the user.
 * @param role            The role of the user in the system.
 */
@JsonPropertyOrder({
        "id",
        "name",
        "surname",
        "date_of_birth",
        "country_of_origin",
        "email",
        "password",
        "phone_number",
        "avatar",
        "status",
        "role"
})
public record UserProfileResponse(
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
