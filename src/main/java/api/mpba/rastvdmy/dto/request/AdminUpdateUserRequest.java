package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AdminUpdateUserRequest(
        String surname,

        @JsonProperty("country_of_origin")
        String countryOfOrigin
) {}
