package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record TimeRequest(
        @JsonProperty("start_date")
        LocalDateTime startDate,
        @JsonProperty("expiration_date")
        LocalDateTime expirationDate
) {
}
