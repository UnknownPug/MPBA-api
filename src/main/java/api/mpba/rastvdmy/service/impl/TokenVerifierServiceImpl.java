package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.TokenVerifierService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service for extracting token and getting user data from the request.
 */
@Service
public class TokenVerifierServiceImpl implements TokenVerifierService {
    private final JwtService jwtService;
    private final UserProfileRepository userProfileRepository;

    /**
     * Constructs a new TokenVerifierServiceImpl with the specified JwtService and UserProfileRepository.
     *
     * @param jwtService            the service for handling JWT operations
     * @param userProfileRepository the repository for user profile operations
     */
    @Autowired
    public TokenVerifierServiceImpl(JwtService jwtService, UserProfileRepository userProfileRepository) {
        this.jwtService = jwtService;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Retrieves the user data for the user identified by the request.
     *
     * @param request the HTTP request containing user information
     * @return the user profile
     * @throws ApplicationException if the user is not authorized or is blocked
     */
    @Override
    public UserProfile getUserData(HttpServletRequest request) throws ApplicationException {
        final String token = jwtService.extractToken(request);
        final String userEmail = jwtService.extractSubject(token);

        UserProfile userProfile = userProfileRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User not authorized."));

        if (UserStatus.STATUS_BLOCKED.equals(userProfile.getStatus())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        return userProfile;
    }
}
