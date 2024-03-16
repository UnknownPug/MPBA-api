package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record DepositRequest(
        @JsonProperty("start_date")
        LocalDateTime startDate,
        @JsonProperty("expiration_date")
        LocalDateTime expirationDate,
        String description,
        Integer referenceNumber
) {
}
