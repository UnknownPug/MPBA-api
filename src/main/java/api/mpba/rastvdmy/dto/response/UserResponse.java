package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({
        "id",
        "name",
        "surname",
        "date_of_birth",
        "country_of_origin",
        "email",
        "password",
        "phone_number",
        "avatar",
        "status",
        "role"
})
public record UserResponse(
        UUID id,

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

        String avatar,

        UserStatus status,

        UserRole role
) {}
