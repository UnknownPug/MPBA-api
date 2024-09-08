package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record AdminUpdateUserRequest(

        @NotBlank(message = "Surname is mandatory")
        String surname,

        @NotBlank(message = "Country of origin is mandatory")
        @JsonProperty("country_of_origin")
        String countryOfOrigin
) {
}
