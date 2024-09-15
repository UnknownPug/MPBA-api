package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a request for a bank identity.
 * @param bankName The name of the bank.
 */
public record BankIdentityRequest(
        @JsonProperty("bank_name")
        String bankName,

        @JsonProperty("bank_number")
        String bankNumber
) {}
