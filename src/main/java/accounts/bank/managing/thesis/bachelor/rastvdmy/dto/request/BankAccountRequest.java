package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
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