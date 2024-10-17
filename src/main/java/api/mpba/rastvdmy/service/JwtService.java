package api.mpba.rastvdmy.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface for managing JWT (JSON Web Token) operations.
 */
public interface JwtService {

    /**
     * Extracts the username from the provided JWT token.
     *
     * @param token the JWT token from which to extract the username
     * @return the username contained in the token
     */
    String extractUsername(String token);

    /**
     * Generates a new JWT token for the specified user details.
     *
     * @param userDetails the user details for whom to generate the token
     * @return the generated JWT token as a string
     */
    String generateToken(UserDetails userDetails);

    /**
     * Retrieves the expiration time for the JWT token in milliseconds.
     *
     * @return the expiration time for the token
     */
    long getExpirationTime();

    /**
     * Validates the provided JWT token against the specified user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to check against
     * @return true if the token is valid for the user details; false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * Extracts the JWT token from the provided HTTP request.
     *
     * @param request the HTTP request containing the token
     * @return the extracted JWT token as a string
     */
    String extractToken(HttpServletRequest request);

    /**
     * Extracts the subject from the provided JWT token.
     *
     * @param token the JWT token from which to extract the subject
     * @return the subject contained in the token
     */
    String extractSubject(String token);
}
