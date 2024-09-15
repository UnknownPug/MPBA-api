package api.mpba.rastvdmy.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record CurrencyDataRequest(
        UUID id,

        String currency,

        BigDecimal rate
) {}
