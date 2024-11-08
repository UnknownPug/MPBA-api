package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.CurrencyDataMapper;
import api.mpba.rastvdmy.dto.request.CurrencyDataRequest;
import api.mpba.rastvdmy.dto.response.CurrencyDataResponse;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.service.CurrencyDataService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing currency data.
 * <p>
 * This controller provides endpoints for users to retrieve and update currency exchange rates.
 * It uses the {@link CurrencyDataService} for business logic and {@link CurrencyDataMapper} for mapping between
 * request and response objects.
 * </p>
 */
@Slf4j
@RestController
@PreAuthorize("hasAnyRole('ROLE_DEFAULT', 'ROLE_ADMIN')")
@RequestMapping("/api/v1/currency-data")
public class CurrencyDataController {

    private final CurrencyDataService currencyDataService;
    private final CurrencyDataMapper currencyDataMapper;

    /**
     * Constructor for CurrencyDataController.
     *
     * @param currencyDataService The {@link CurrencyDataService} to be used.
     * @param currencyDataMapper  The {@link CurrencyDataMapper} to be used.
     */
    public CurrencyDataController(CurrencyDataService currencyDataService, CurrencyDataMapper currencyDataMapper) {
        this.currencyDataService = currencyDataService;
        this.currencyDataMapper = currencyDataMapper;
    }

    /**
     * Updates and retrieves all currency data.
     *
     * @param request The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} containing a list of {@link CurrencyDataResponse}.
     */
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<CurrencyDataResponse>> updateAndFetchAllCurrencies(HttpServletRequest request) {
        logInfo("Updating currency data ...");
        currencyDataService.findAllExchangeRates();
        List<CurrencyData> currencies = currencyDataService.findAllCurrencies(request);
        List<CurrencyDataResponse> currencyDataResponses = currencies.stream()
                .map(currencyData -> currencyDataMapper.toResponse(convertToCurrencyRequest(currencyData)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(currencyDataResponses);
    }

    /**
     * Retrieves currency data by currency type.
     *
     * @param request      The HTTP servlet request containing user information.
     * @param currencyType The type of currency to retrieve.
     * @return A {@link ResponseEntity} containing the requested {@link CurrencyDataResponse}.
     */
    @GetMapping(path = "/{currency}", produces = "application/json")
    public ResponseEntity<CurrencyDataResponse> findByCurrency(
            HttpServletRequest request,
            @PathVariable(value = "currency") String currencyType) {
        logInfo("Getting currency {} ...", currencyType);
        currencyDataService.findAllExchangeRates();
        CurrencyData currencyData = currencyDataService.findByCurrency(request, currencyType);
        CurrencyDataResponse currencyDataResponse = currencyDataMapper.toResponse(
                convertToCurrencyRequest(currencyData));
        return ResponseEntity.ok(currencyDataResponse);
    }

    /**
     * Converts a {@link CurrencyData} object to a {@link CurrencyDataRequest} object.
     *
     * @param currencyData The {@link CurrencyData} object to convert.
     * @return A {@link CurrencyDataRequest} object.
     */
    private static CurrencyDataRequest convertToCurrencyRequest(CurrencyData currencyData) {
        return new CurrencyDataRequest(
                currencyData.getCurrency(),
                currencyData.getRate()
        );
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        log.info(message, args);
    }
}