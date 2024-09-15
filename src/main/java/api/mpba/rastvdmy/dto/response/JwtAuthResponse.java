package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record JwtAuthResponse(
        @JsonProperty("expires_in")
        long expiresIn,

        String token
) {}
