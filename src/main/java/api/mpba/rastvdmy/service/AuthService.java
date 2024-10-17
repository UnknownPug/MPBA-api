package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;

/**
 * Service interface for authentication operations, including user sign-up and authentication.
 */
public interface AuthService {

    /**
     * Signs up a new user with the provided user profile information.
     *
     * @param userProfileRequest the request object containing user profile information
     * @return a {@link JwtAuthResponse} containing the JWT and other authentication details
     * @throws Exception if an error occurs during the sign-up process
     */
    JwtAuthResponse signUp(UserProfileRequest userProfileRequest) throws Exception;

    /**
     * Authenticates a user with the provided login credentials.
     *
     * @param loginRequest the request object containing login credentials
     * @return a {@link JwtAuthResponse} containing the JWT and other authentication details
     */
    JwtAuthResponse authenticate(UserLoginRequest loginRequest);
}
