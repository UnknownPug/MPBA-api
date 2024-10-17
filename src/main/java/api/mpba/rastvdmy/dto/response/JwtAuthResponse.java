package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * This class represents the response for JWT authentication.
 *
 * @param expiresIn The duration (in seconds) until the token expires.
 * @param token     The JWT token issued for the authenticated session.
 */
@Builder
public record JwtAuthResponse(
        @JsonProperty("expires_in")
        long expiresIn,

        String token
) {}
