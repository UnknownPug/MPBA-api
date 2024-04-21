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

@Slf4j
@RestController
@RequestMapping("/api/currency-data")
public class CurrencyDataController {
    private static final Logger LOG = LoggerFactory.getLogger(CurrencyDataController.class);

    private final CurrencyDataService currencyDataService;

    @Autowired
    public CurrencyDataController(CurrencyDataService currencyDataService) {
        this.currencyDataService = currencyDataService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MODERATOR')")
    public ResponseEntity<List<CurrencyData>> updateAndFetchAllCurrencies() {
        LOG.info("Updating currency data ...");
        currencyDataService.findAllExchangeRates();
        return ResponseEntity.ok(currencyDataService.findAllCurrencies());
    }

    @GetMapping(path = "/{currency}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER', 'ROLE_MODERATOR')")
    public ResponseEntity<CurrencyData> findByCurrency(@PathVariable(value = "currency") String currencyType) {
        LOG.info("Getting currency {} ...", currencyType);
        currencyDataService.findAllExchangeRates();
        return ResponseEntity.ok(currencyDataService.findByCurrency(currencyType));
    }
}
