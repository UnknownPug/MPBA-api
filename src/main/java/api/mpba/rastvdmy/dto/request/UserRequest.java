package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserRequest(
        String name,

        String surname,

        @JsonProperty("date_of_birth")
        String dateOfBirth,

        @JsonProperty("country_of_origin")
        String countryOfOrigin,

        String email,

        String password,

        @JsonProperty("phone_number")
        String phoneNumber,

        String avatar
) {}
