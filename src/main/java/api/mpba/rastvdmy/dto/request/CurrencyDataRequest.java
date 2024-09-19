package api.mpba.rastvdmy.dto.request;

import java.math.BigDecimal;

public record CurrencyDataRequest(
        String currency,

        BigDecimal rate
) {}
