package api.mpba.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record BankAccountRequest(

        @NotNull(message = "Balance is required")
        @Positive(message = "Balance must be positive")
        BigDecimal balance,

        @NotBlank(message = "User bank account number is mandatory")
        @JsonProperty(namespace = "account_number")
        String accountNumber,

        @Nullable
        String swift,

        @Nullable
        String iban
) {}