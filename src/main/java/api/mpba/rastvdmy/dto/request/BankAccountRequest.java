package api.mpba.rastvdmy.dto.request;

import api.mpba.rastvdmy.entity.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BankAccountRequest(
        @JsonProperty("account_number")
        String accountNumber,

        BigDecimal balance,

        Currency currency,

        String swift,

        String iban
) {}