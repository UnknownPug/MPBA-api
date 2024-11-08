package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for user validation services.
 */
public interface TokenVerifierService {
    /**
     * Retrieves the user data for the user identified by the request.
     *
     * @param request               the HTTP request containing user information
     * @return the user profile
     * @throws ApplicationException if the user is not found or is blocked
     */
    UserProfile getUserData(HttpServletRequest request) throws ApplicationException;
}