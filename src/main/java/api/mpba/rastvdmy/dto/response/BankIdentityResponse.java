package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bank identity response
 * @param bankName The name of the bank
 */
public record BankIdentityResponse(
        @JsonProperty("bank_name")
        String bankName,

        @JsonProperty("bank_number")
        String bankNumber,

        String swift
){}
