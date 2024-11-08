package api.mpba.rastvdmy.service.validator;

import api.mpba.rastvdmy.exception.ApplicationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.spi.exception.RestClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * A strategy for validating a country by checking if a country exists by checking against an external REST API.
 */
@Component
public class CountryValidation implements ValidationStrategy {
    private final RestTemplate restTemplate;

    /**
     * Constructor for CountryValidation.
     *
     * @param restTemplate The RestTemplate to use for making HTTP requests.
     */
    public CountryValidation(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Validates the given country name.
     *
     * @param countryName The name of the country to validate.
     * @throws ApplicationException if the country does not exist.
     */
    @Override
    public void validate(String countryName) throws ApplicationException {
        if (countryDoesNotExist(countryName)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
    }

    /**
     * Checks if the given country name does not exist by querying an external REST API.
     *
     * @param countryName The name of the country to check.
     * @return true if the country does not exist, false otherwise.
     */
    private boolean countryDoesNotExist(String countryName) {
        final String url = "https://restcountries.com/v3.1/all?fields=name";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            // Check if the response status is successful and the body is not null
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());
                // Check if the response body is an array of countries
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        JsonNode nameNode = node.get("name");
                        if (nameNode != null && nameNode.get("common") != null) {
                            String commonName = nameNode.get("common").asText();
                            // Check if the common name matches the country name (case-insensitive)
                            if (commonName.equalsIgnoreCase(countryName)) {
                                return false; // Country exists
                            }
                        }
                    }
                }
            }
        } catch (RestClientException | IOException e) {
            e.getCause(); // Log the cause of the exception (if needed)
        }
        return true; // The Country does not exist
    }
}