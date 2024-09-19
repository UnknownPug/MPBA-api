package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.CurrencyDataRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.CurrencyDataService;
import api.mpba.rastvdmy.service.JwtService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
 * This class is responsible for managing currency data.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses CurrencyDataRepository to interact with the database.
 * It also uses RestTemplate to make HTTP requests to an external API.
 */
@Service
public class CurrencyDataServiceImpl implements CurrencyDataService {
    private final CurrencyDataRepository currencyDataRepository;
    private final RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${api.key}")
    private String apiKey;

    /**
     * Constructs a new CurrencyDataServiceImpl with the given repository and RestTemplate.
     *
     * @param currencyDataRepository The CurrencyDataRepository to use.
     * @param restTemplate           The RestTemplate to use.
     */
    @Autowired
    public CurrencyDataServiceImpl(CurrencyDataRepository currencyDataRepository,
                                   RestTemplate restTemplate, JdbcTemplate jdbcTemplate,
                                   JwtService jwtService, UserRepository userRepository) {
        this.currencyDataRepository = currencyDataRepository;
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all currency data.
     *
     * @return A list of all currency data.
     */
    @Cacheable(value = "currencies")
    public List<CurrencyData> findAllCurrencies(HttpServletRequest request) {
        isUserValid(request);
        return currencyDataRepository.findAll();
    }

    /**
     * Retrieves currency data by its type.
     *
     * @param currencyType The type of the currency to retrieve.
     * @return The retrieved currency data.
     */
    @Cacheable(value = "currencies", key = "#currencyType")
    public CurrencyData findByCurrency(HttpServletRequest request, String currencyType) {
        isUserValid(request);
        if (currencyType.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Specify currency type.");
        }
        List<CurrencyData> currencyDataList = currencyDataRepository.findAllByCurrency(currencyType);
        if (!currencyDataList.isEmpty()) {
            return currencyDataList.getFirst();
        } else {
            CurrencyData currencyData = getCurrencyFromApi(currencyType);
            if (currencyData != null) {
                currencyData = currencyDataRepository.save(currencyData);
                return currencyData;
            } else {
                throw new ApplicationException(HttpStatus.NOT_FOUND, "Currency " + currencyType + " is not found.");
            }
        }
    }

    private void isUserValid(HttpServletRequest request) {
        BankAccountServiceImpl.getUserData(request, jwtService, userRepository);
    }

    /**
     * Retrieves currency data from an external API.
     *
     * @param currencyType The type of the currency to retrieve.
     * @return The retrieved currency data.
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
     * Converts a currency to another currency.
     *
     * @param baseCurrency   The currency to convert from.
     * @param targetCurrency The currency to convert to.
     * @return The converted currency data.
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
     * Initializes the database with currency data.
     * Connecting admin with all currency data.
     */
    @PostConstruct
    public void initializeData() {
        findAllExchangeRates();
        // Check if the admin user already has connections in the user_profile_currency_data table
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM user_profile_currency_data " +
                        "WHERE users_id = '00000000-0000-0000-0000-000000000001'",
                Integer.class
        );
        if (count == null || count == 0) {
            // Run the SQL script to connect the admin with the currencies
            jdbcTemplate.execute(
                    "INSERT INTO user_profile_currency_data(users_id, currency_data_id)" +
                            " SELECT '00000000-0000-0000-0000-000000000001', id" +
                            " FROM currency_data;"
            );
        }
    }

    /**
     * Retrieves all exchange rates from an external API and updates the database.
     * This method is scheduled to run every 24 hours.
     */
    @CacheEvict(value = "currencies", allEntries = true)
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