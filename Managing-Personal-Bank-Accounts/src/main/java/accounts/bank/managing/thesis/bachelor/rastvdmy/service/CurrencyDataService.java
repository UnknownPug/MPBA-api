package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CurrencyData;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class CurrencyDataService {
    private final CurrencyDataRepository currencyDataRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public CurrencyDataService(CurrencyDataRepository currencyDataRepository, UserRepository userRepository, RestTemplate restTemplate) {
        this.currencyDataRepository = currencyDataRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 180000) // Update every 3 minutes
    public void updateCurrencyData() {
        String apiUrl = "https://api.exchangeratesapi.io/latest?symbols="
                + Currency.CZK + Currency.UAH + Currency.PLN + Currency.USD + Currency.EUR;
        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        Map<String, Object> response = responseEntity.getBody();
        if ((response != null && response.containsKey("rates")) && response.get("rates") instanceof Map<?, ?> ratesMap) {
            for (Map.Entry<?, ?> entry : ratesMap.entrySet()) {
                if (entry.getKey() instanceof String currency && entry.getValue() instanceof Double rate) {
                    CurrencyData currencyData = new CurrencyData();
                    currencyData.setCurrency(currency);
                    currencyData.setRate(rate);

                    userRepository.updateCurrencyDataForAllUsers(currencyData);

                    List<User> users = userRepository.findAllByCurrencyData(currencyData);
                    currencyData.setUsers(users);

                    currencyDataRepository.save(currencyData);
                } else {
                    throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update currency data.");
                }
            }
        } else {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update currency data.");
        }
    }

    public CurrencyData findByCurrency(String currencyType) {
        if (currencyType.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Specify currency type.");
        }
        if (currencyExists(currencyType)) {
            return currencyDataRepository.findByCurrency(currencyType);
        } else {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Currency " + currencyType + " is not found.");
        }
    }

    private boolean currencyExists(String currencyType) {
        final String url = "https://api.exchangeratesapi.io/latest?symbols=" + currencyType;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null && !response.getBody().isEmpty();
        } catch (RestClientException e) {
            return false;
        }
    }

    public List<CurrencyData> findAllCurrencies() {
        return currencyDataRepository.findAll();
    }
}