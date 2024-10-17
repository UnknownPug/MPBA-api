package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * This class represents the response for a bank account in the system.
 *
 * @param id            The unique identifier of the bank account.
 * @param accountNumber The number associated with the bank account.
 * @param balance       The current balance of the bank account.
 * @param currency      The currency used for the bank account.
 * @param iban          The IBAN (International Bank Account Number) of the account.
 */
@JsonPropertyOrder({"id", "currency", "balance", "account_number", "iban"})
public record BankAccountResponse(
        UUID id,

        @JsonProperty("account_number")
        String accountNumber,

        BigDecimal balance,

        Currency currency,

        String iban
) {}
