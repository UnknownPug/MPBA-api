package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record BankAccountRequest(
        UUID id,

        @JsonProperty("account_number")
        String accountNumber,

        BigDecimal balance,

        Currency currency,

        String iban
) {}