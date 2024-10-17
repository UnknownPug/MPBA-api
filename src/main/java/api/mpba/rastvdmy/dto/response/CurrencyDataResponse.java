package api.mpba.rastvdmy.dto.response;

import java.math.BigDecimal;

/**
 * This class represents the response for currency data.
 *
 * @param currency The currency code (e.g., USD, EUR).
 * @param rate     The exchange rate of the currency.
 */
public record CurrencyDataResponse(
        String currency,

        BigDecimal rate
) {}
