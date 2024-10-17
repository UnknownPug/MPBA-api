package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a request to update a user's profile information.
 *
 * @param email       The user's email address.
 * @param password    The user's new password.
 * @param phoneNumber The user's updated phone number.
 */
public record UserUpdateRequest(
        String email,

        String password,

        @JsonProperty("phone_number")
        String phoneNumber
) {}