package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserUpdateRequest(
        String email,

        String password,

        @JsonProperty("phone_number")
        String phoneNumber
) {
}
