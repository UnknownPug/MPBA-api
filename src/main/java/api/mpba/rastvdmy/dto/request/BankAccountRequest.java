package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BankAccountRequest(
        BigDecimal balance,

        @JsonProperty("account_number")
        String accountNumber,

        String swift,

        String iban
) {}