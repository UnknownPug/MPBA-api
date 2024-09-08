package api.mpba.rastvdmy.dto.response;

/**
 * This class represents a response for a user.
 *
 * @param name            The name of the user.
 * @param surname         The surname of the user.
 * @param dateOfBirth     The date of birth of the user.
 * @param countryOfOrigin The country of origin of the user.
 * @param email           The email of the user.
 * @param password        The password of the user.
 * @param phoneNumber     The phone number of the user.
 */
public record UserResponse(
        String name,

        String surname,

        String dateOfBirth,

        String countryOfOrigin,

        String email,

        String password,

        String phoneNumber,

        String avatar
) {}
