package api.mpba.rastvdmy.dto.request;

/**
 * This class represents a request for user login.
 *
 * @param email    The email address of the user attempting to log in.
 * @param password The password associated with the user's account.
 */
public record UserLoginRequest(
        String email,

        String password
) {}
