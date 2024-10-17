package api.mpba.rastvdmy.dto.request;

import java.math.BigDecimal;

/**
 * This class represents a request for currency data, including the currency
 * code and the corresponding exchange rate.
 *
 * @param currency The currency code (e.g., "USD", "EUR").
 * @param rate     The exchange rate for the specified currency.
 */
public record CurrencyDataRequest(
        String currency,

        BigDecimal rate
) {}
