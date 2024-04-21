package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * This class is responsible for managing currency data.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses CurrencyDataRepository to interact with the database.
 * It also uses RestTemplate to make HTTP requests to an external API.
 */
@Service
public class CurrencyDataService {
    private final CurrencyDataRepository currencyDataRepository;
    private final RestTemplate restTemplate;

    @Value("${api.key}")
    private String apiKey;

    /**
     * Constructs a new CurrencyDataService with the given repository and RestTemplate.
     *
     * @param currencyDataRepository The CurrencyDataRepository to use.
     * @param restTemplate The RestTemplate to use.
     */
    @Autowired
    public CurrencyDataService(CurrencyDataRepository currencyDataRepository, RestTemplate restTemplate) {
        this.currencyDataRepository = currencyDataRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves all currency data.
     *
     * @return A list of all currency data.
     */
    @Cacheable(value = "currencies")
    public List<CurrencyData> findAllCurrencies() {
        return currencyDataRepository.findAll();
    }

    /**
     * Retrieves currency data by its type.
     *
     * @param currencyType The type of the currency to retrieve.
     * @return The retrieved currency data.
     */
    @Cacheable(value = "currencies", key = "#currencyType")
    public CurrencyData findByCurrency(String currencyType) {
        if (currencyType.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Specify currency type.");
        }
        List<CurrencyData> currencyDataList = currencyDataRepository.findAllByCurrency(currencyType);
        if (!currencyDataList.isEmpty()) {
            return currencyDataList.get(0);
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
                        currencyData.setRate(conversionRates.get(currencyType).doubleValue());
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
     * Retrieves all exchange rates from an external API and updates the database.
     * This method is scheduled to run every 24 hours.
     */
    @CacheEvict(value = "currencies", allEntries = true)
    @Scheduled(fixedRate = 86400000) // Update every 24 hours
    @PostConstruct
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
                    currencyData.setRate(entry.getValue().doubleValue());
                } else {
                    // Create a new CurrencyData
                    currencyData = new CurrencyData();
                    currencyData.setCurrency(entry.getKey());
                    currencyData.setRate(entry.getValue().doubleValue());
                }
                currencyDataRepository.save(currencyData);
            }
        } else {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch exchange rates.");
        }
    }
}