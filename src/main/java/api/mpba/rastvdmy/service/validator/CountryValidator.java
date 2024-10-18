package api.mpba.rastvdmy.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.spi.exception.RestClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * A utility class that validates if a country exists by checking against an external REST API.
 */
@Component
public class CountryValidator {
    private final RestTemplate restTemplate;

    /**
     * Constructs a CountryValidator with the given RestTemplate.
     *
     * @param restTemplate the RestTemplate used to make HTTP requests
     */
    public CountryValidator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Checks if a country exists by making a request to the REST countries API.
     *
     * @param countryName the name of the country to check
     * @return {@code true} if the country does not exist; {@code false} if it exists
     */
    public boolean countryExists(String countryName) {
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