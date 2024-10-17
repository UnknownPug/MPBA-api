package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents the response for a bank identity in the system.
 *
 * @param bankName   The name of the bank.
 * @param bankNumber The bank's identification number.
 * @param swift      The SWIFT code of the bank.
 */
public record BankIdentityResponse(
        @JsonProperty("bank_name")
        String bankName,

        @JsonProperty("bank_number")
        String bankNumber,

        String swift
) {}
