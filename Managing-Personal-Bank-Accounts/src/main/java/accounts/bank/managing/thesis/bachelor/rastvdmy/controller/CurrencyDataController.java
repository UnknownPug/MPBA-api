package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CurrencyDataService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This class is responsible for handling currency data related requests.
 * It provides endpoints for updating and fetching all currencies and finding a currency by its type.
 */
@Slf4j
@RestController
@RequestMapping("/api/currency-data")
public class CurrencyDataController {
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyDataController.class);

    private final CurrencyDataService currencyDataService;

    /**
     * Constructor for the CurrencyDataController.
     *
     * @param currencyDataService The service to handle currency data operations.
     */
    @Autowired
    public CurrencyDataController(CurrencyDataService currencyDataService) {
        this.currencyDataService = currencyDataService;
    }

    /**
     * This method is used to update and fetch all currencies.
     *
     * @return A list of all currency data.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MODERATOR')")
    public ResponseEntity<List<CurrencyData>> updateAndFetchAllCurrencies() {
        LOG.info("Updating currency data ...");
        currencyDataService.findAllExchangeRates();
        return ResponseEntity.ok(currencyDataService.findAllCurrencies());
    }

    /**
     * This method is used to find a currency by its type.
     *
     * @param currencyType The type of the currency.
     * @return The currency data of the given type.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{currency}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MODERATOR')")
    public ResponseEntity<CurrencyData> findByCurrency(@PathVariable(value = "currency") String currencyType) {
        LOG.info("Getting currency {} ...", currencyType);
        currencyDataService.findAllExchangeRates();
        return ResponseEntity.ok(currencyDataService.findByCurrency(currencyType));
    }
}
