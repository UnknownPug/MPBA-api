package accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

public record CurrencyDataRequest(
        @NotBlank
        UUID id,

        @NotBlank
        String currency,

        @NotBlank
        BigDecimal rate
) {}
