package api.mpba.rastvdmy.dto.response;

import java.math.BigDecimal;

public record CurrencyDataResponse(
        String currency,

        BigDecimal rate
) {}
