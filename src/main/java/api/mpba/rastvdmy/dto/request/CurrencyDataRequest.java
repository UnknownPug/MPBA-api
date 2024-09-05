package api.mpba.rastvdmy.dto.request;

import jakarta.validation.constraints.NotBlank;
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
