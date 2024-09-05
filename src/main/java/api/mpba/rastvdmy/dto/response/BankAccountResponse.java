package api.mpba.rastvdmy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * This class represents a bank account in the banking system.
 * @param accountNumber The account number of the bank account.
 */
public record BankAccountResponse(
        @JsonProperty("account_number")
        String accountNumber,

        BigDecimal balance,

        String swift,

        String iban
) {}
