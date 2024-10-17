package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.CurrencyData;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service for managing currency data and operations.
 */
public interface CurrencyDataService {

    /**
     * Finds all available currencies.
     *
     * @param request the HTTP request containing user information
     * @return a list of all available {@link CurrencyData}
     */
    List<CurrencyData> findAllCurrencies(HttpServletRequest request);

    /**
     * Finds currency data by the specified currency code.
     *
     * @param request  the HTTP request containing user information
     * @param currency the currency code for which to retrieve data
     * @return the {@link CurrencyData} associated with the specified currency code
     */
    CurrencyData findByCurrency(HttpServletRequest request, String currency);

    /**
     * Converts an amount from a base currency to a target currency.
     *
     * @param baseCurrency   the currency to convert from
     * @param targetCurrency the currency to convert to
     * @return the {@link CurrencyData} containing the conversion result
     */
    CurrencyData convertCurrency(String baseCurrency, String targetCurrency);

    /**
     * Finds and retrieves all exchange rates.
     * This method may return void if it only updates the internal state
     * or performs side effects without returning data.
     */
    void findAllExchangeRates();
}
