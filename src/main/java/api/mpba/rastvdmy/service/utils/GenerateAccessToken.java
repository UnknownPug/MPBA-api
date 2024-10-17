package api.mpba.rastvdmy.service.utils;

import api.mpba.rastvdmy.entity.AccessToken;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.repository.AccessTokenRepository;
import api.mpba.rastvdmy.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * A service class for generating access tokens for user profiles.
 */
@Component
public class GenerateAccessToken {

    private final JwtService jwtService;
    private final AccessTokenRepository tokenRepository;

    /**
     * Constructs a GenerateAccessToken instance with the specified JwtService
     * and AccessTokenRepository.
     *
     * @param jwtService the service used for generating JWT tokens
     * @param tokenRepository the repository for storing access tokens
     */
    @Autowired
    public GenerateAccessToken(JwtService jwtService, AccessTokenRepository tokenRepository) {
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Generates an access token for the given user profile.
     *
     * @param userProfile the user profile for which the access token will be generated
     * @return TokenDetails containing the generated token and its expiration time
     */
    public TokenDetails generate(UserProfile userProfile) {
        // Generate the token using the JwtService
        String token = jwtService.generateToken(userProfile);
        long tokenExpiration = jwtService.getExpirationTime();

        // Create an AccessToken entity to save in the repository
        AccessToken accessToken = AccessToken.builder()
                .id(UUID.randomUUID()) // Generate a unique identifier for the access token
                .token(token)
                .userProfile(userProfile)
                .expirationDate(LocalDateTime.now().plus(tokenExpiration, ChronoUnit.MILLIS)) // Set expiration date
                .build();

        // Save the access token in the repository
        tokenRepository.save(accessToken);

        // Return the token details
        return new TokenDetails(token, tokenExpiration);
    }

    /**
     * A record representing the details of the generated token.
     *
     * @param token the generated token
     * @param tokenExpiration the expiration time of the token in milliseconds
     */
    public record TokenDetails(String token, long tokenExpiration) {}
}