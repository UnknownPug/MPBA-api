package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A Data Transfer Object (DTO) that represents a request for a bank account.
 * This record encapsulates the essential information needed to create or
 * update a bank account.
 *
 * <p>The following fields are included:</p>
 *
 * <ul>
 *   <li><b>id</b>: The unique identifier of the bank account, represented as
 *       a UUID.</li>
 *   <li><b>accountNumber</b>: The account number of the bank account,
 *       serialized to/from JSON with the key "account_number".</li>
 *   <li><b>balance</b>: The current balance of the bank account, represented
 *       as a BigDecimal.</li>
 *   <li><b>currency</b>: The currency of the bank account, represented as a
 *       Currency object.</li>
 *   <li><b>iban</b>: The International Bank Account Number (IBAN) for the
 *       bank account.</li>
 * </ul>
 *
 * @param id            The unique identifier of the bank account.
 * @param accountNumber The account number of the bank account.
 * @param balance       The current balance of the bank account.
 * @param currency      The currency of the bank account.
 * @param iban          The International Bank Account Number (IBAN) for the
 *                      bank account.
 */
public record BankAccountRequest(
        UUID id,

        @JsonProperty("account_number")
        String accountNumber,

        BigDecimal balance,

        Currency currency,

        String iban
) {}
