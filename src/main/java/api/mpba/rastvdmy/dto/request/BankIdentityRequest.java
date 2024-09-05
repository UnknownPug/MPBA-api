package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

/**
 * This class represents a request for a bank identity.
 * @param bankName The name of the bank.
 */
@Builder
public record BankIdentityRequest(
        @NotBlank(message = "Bank name is mandatory")
        @JsonProperty(namespace = "bank_name")
        String bankName
) {}
