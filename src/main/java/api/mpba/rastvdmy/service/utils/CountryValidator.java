package api.mpba.rastvdmy.service.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.spi.exception.RestClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Component
public class CountryValidator {
    private final RestTemplate restTemplate;

    public CountryValidator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean countryExists(String countryName) {
        final String url = "https://restcountries.com/v3.1/all?fields=name";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        JsonNode nameNode = node.get("name");
                        if (nameNode != null && nameNode.get("common") != null) {
                            String commonName = nameNode.get("common").asText();
                            if (commonName.equalsIgnoreCase(countryName)) {
                                return false;
                            }
                        }
                    }
                }
            }
        } catch (RestClientException | IOException e) {
            e.getCause();
        }
        return true;
    }
}