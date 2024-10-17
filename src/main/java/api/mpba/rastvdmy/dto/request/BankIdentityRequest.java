package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a request for a bank identity.
 *
 * <p>It includes the following fields:</p>
 * <ul>
 *   <li><b>bankName</b>: The name of the bank, serialized to/from JSON with
 *       the key "bank_name".</li>
 *   <li><b>bankNumber</b>: The unique identifier for the bank, serialized
 *       to/from JSON with the key "bank_number".</li>
 *   <li><b>swift</b>: The SWIFT code of the bank.</li>
 * </ul>
 *
 * @param bankName   The name of the bank.
 * @param bankNumber The unique identifier for the bank.
 * @param swift      The SWIFT code of the bank.
 */
public record BankIdentityRequest(
        @JsonProperty("bank_name")
        String bankName,

        @JsonProperty("bank_number")
        String bankNumber,

        String swift
) {}
