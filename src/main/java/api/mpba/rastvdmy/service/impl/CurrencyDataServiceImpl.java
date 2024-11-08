package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.CurrencyDataRepository;
import api.mpba.rastvdmy.service.CurrencyDataService;
import api.mpba.rastvdmy.service.UserValidationService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for managing currency data.
 * This service handles retrieving currency information from the database and external APIs.
 */
@Service
public class CurrencyDataServiceImpl implements CurrencyDataService {
    private final CurrencyDataRepository currencyDataRepository;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final UserValidationService userValidationService;

    @Value("${api.key}")
    private String apiKey; // API key for the external currency API

    /**
     * Constructs a new CurrencyDataServiceImpl with the specified repositories, RestTemplate, JdbcTemplate, JwtService,
     * and UserProfileRepository.
     *
     * @param currencyDataRepository the repository for currency data
     * @param restTemplate           the RestTemplate for making HTTP requests
     * @param jdbcTemplate           the JdbcTemplate for executing SQL queries
     * @param userValidationService  the service for extracting user token and getting user data from the request
     */
    @Autowired
    public CurrencyDataServiceImpl(CurrencyDataRepository currencyDataRepository,
                                   RestTemplate restTemplate,
                                   JdbcTemplate jdbcTemplate,
                                   UserValidationService userValidationService) {
        this.currencyDataRepository = currencyDataRepository;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.userValidationService = userValidationService;
    }

    /**
     * Retrieves all available currencies.
     *
     * @param request the HTTP request containing user data
     * @return a list of all available currencies
     */
    public List<CurrencyData> findAllCurrencies(HttpServletRequest request) {
        userValidationService.getUserData(request);
        return currencyDataRepository.findAll();
    }

    /**
     * Retrieves currency data for a specified currency type.
     * If the currency is not found in the database, it fetches it from an external API.
     *
     * @param request      the HTTP request containing user data
     * @param currencyType the type of currency to retrieve
     * @return the currency data for the specified type
     * @throws ApplicationException if the specified currency type is invalid or not found
     */
    public CurrencyData findByCurrency(HttpServletRequest request, String currencyType) {
        userValidationService.getUserData(request);
        if (currencyType.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Specify currency type.");
        }
        List<CurrencyData> currencyDataList = currencyDataRepository.findAllByCurrency(currencyType.trim());
        if (!currencyDataList.isEmpty()) {
            return currencyDataList.getFirst();
        } else {
            CurrencyData currencyData = getCurrencyFromApi(currencyType.trim());
            if (currencyData != null) {
                currencyData = currencyDataRepository.save(currencyData);
                return currencyData;
            } else {
                throw new ApplicationException(HttpStatus.NOT_FOUND, "Currency " + currencyType + " is not found.");
            }
        }
    }

    /**
     * Fetches currency data from an external API.
     *
     * @param currencyType the currency type to retrieve
     * @return the currency data or null if not found
     */
    private CurrencyData getCurrencyFromApi(String currencyType) {
        final String apiUrl = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/" + Currency.CZK;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("conversion_rates")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Number> conversionRates = (Map<String, Number>) responseBody.get("conversion_rates");
                    if (conversionRates.containsKey(currencyType)) {
                        CurrencyData currencyData = new CurrencyData();
                        currencyData.setCurrency(currencyType);
                        currencyData.setRate(BigDecimal.valueOf(conversionRates.get(currencyType).doubleValue()));
                        return currencyData;
                    }
                }
            }
        } catch (RestClientException e) {
            e.getCause();
        }
        return null;
    }

    /**
     * Converts an amount from the base currency to the target currency using an external API.
     *
     * @param baseCurrency   the currency from which to convert
     * @param targetCurrency the currency to which to convert
     * @return the currency data for the target currency
     */
    public CurrencyData convertCurrency(String baseCurrency, String targetCurrency) {
        final String apiUrl =
                "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/" + baseCurrency + "/" + targetCurrency;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody.containsKey("conversion_rate")) {
                    CurrencyData currencyData = new CurrencyData();
                    currencyData.setCurrency(targetCurrency);
                    currencyData.setRate(
                            BigDecimal.valueOf(((Number) responseBody.get("conversion_rate")).doubleValue())
                    );
                    return currencyData;
                }
            }
        } catch (RestClientException e) {
            e.getCause();
        }
        return null;
    }

    /**
     * Initializes the database with currency data and connects the admin user to all currencies.
     */
    @PostConstruct
    public void initializeData() {
        findAllExchangeRates();
        // Check if the admin user already has connections in the user_profile_currency_data table
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM user_profile_currency_data " +
                        "WHERE user_profiles_id = '00000000-0000-0000-0000-000000000001'",
                Integer.class
        );
        if (count == null || count == 0) {
            // Run the SQL script to connect the admin with the currencies
            jdbcTemplate.execute(
                    "INSERT INTO user_profile_currency_data(user_profiles_id, currency_data_id) " +
                            "SELECT '00000000-0000-0000-0000-000000000001', id " +
                            "FROM currency_data;"
            );
        }
    }

    /**
     * Retrieves all exchange rates from an external API and updates the database.
     * This method is scheduled to run every 24 hours.
     */
    @Scheduled(fixedRate = 86400000) // Update every 24 hours
    public void findAllExchangeRates() {
        String apiUrl = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/CZK";
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        Map<String, Object> response = responseEntity.getBody();
        if (response != null && response.containsKey("conversion_rates")) {
            @SuppressWarnings("unchecked")
            Map<String, Number> ratesMap = (Map<String, Number>) response.get("conversion_rates");

            for (Map.Entry<String, Number> entry : ratesMap.entrySet()) {
                CurrencyData currencyData = currencyDataRepository.findByCurrency(entry.getKey());
                if (currencyData != null) {
                    // Update the existing CurrencyData
                    currencyData.setRate(BigDecimal.valueOf(entry.getValue().doubleValue()));
                } else {
                    // Create a new CurrencyData
                    currencyData = new CurrencyData();
                    currencyData.setCurrency(entry.getKey());
                    currencyData.setRate(BigDecimal.valueOf(entry.getValue().doubleValue()));
                }
                currencyDataRepository.save(currencyData);
            }
        } else {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch exchange rates.");
        }
    }
}