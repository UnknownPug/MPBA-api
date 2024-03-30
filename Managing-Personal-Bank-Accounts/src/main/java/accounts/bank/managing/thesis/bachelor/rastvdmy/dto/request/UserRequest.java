package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record UserRequest(
        String name,
        String surname,
        @JsonProperty("date_of_birth")
        LocalDate dateOfBirth,
        String countryOfOrigin,
        String email,
        String password,
        String phoneNumber) {

}
