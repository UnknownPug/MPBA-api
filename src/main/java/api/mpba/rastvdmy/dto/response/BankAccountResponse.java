package api.mpba.rastvdmy.dto.response;

import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.util.UUID;

@JsonPropertyOrder({"id", "currency", "balance", "account_number", "iban"})
public record BankAccountResponse(
        UUID id,

        @JsonProperty("account_number")
        String accountNumber,

        BigDecimal balance,

        Currency currency,

        String iban
) {}
