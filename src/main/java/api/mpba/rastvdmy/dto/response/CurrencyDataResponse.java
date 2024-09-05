package api.mpba.rastvdmy.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CurrencyDataResponse(
        UUID id,

        String currency,

        BigDecimal rate
) {}
