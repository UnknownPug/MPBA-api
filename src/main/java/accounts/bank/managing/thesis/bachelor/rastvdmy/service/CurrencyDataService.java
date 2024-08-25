package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service for currency data.
 */
public interface CurrencyDataService {

    /**
     * Find all currencies.
     * @return list of all currencies
     */
    List<CurrencyData> findAllCurrencies(HttpServletRequest request);

    /**
     * Find currency by currency.
     * @param currency currency
     * @return currency data
     */
    CurrencyData findByCurrency(HttpServletRequest request, String currency);

    CurrencyData convertCurrency(String baseCurrency, String targetCurrency);

    /**
     * Find all exchange rates.
     */
    void findAllExchangeRates();
}
