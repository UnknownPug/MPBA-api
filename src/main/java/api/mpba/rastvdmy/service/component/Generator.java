package api.mpba.rastvdmy.service.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.spi.exception.RestClientException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is responsible for generating various types of random strings.
 * It is annotated with @Component to indicate that it's a Spring managed bean.
 */
@Component
public class Generator {
    private final RestTemplate restTemplate;

    public Generator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateBankNumber() {
        Random random = new Random();
        StringBuilder bankNumber = new StringBuilder();
        // Generate 4 random digits for the bank number
        for (int i = 0; i < 4; i++) {
            bankNumber.append(random.nextInt(10));
        }
        return bankNumber.toString();
    }

    /**
     * Generates an IBAN (International Bank Account Number) with a specific format.
     * The format is "CZ" followed by 2 random digits, "CVUT", 6 random digits, and 10 random digits.
     *
     * @return The generated IBAN.
     */
    public String generateIban() {
        Random random = new Random();
        StringBuilder iban = new StringBuilder("CZ");
        // Generate 2 random digits for the country code
        for (int i = 0; i < 2; i++) {
            iban.append(random.nextInt(10));
        }
        // Generate 4 uppercase letters for the bank code
        iban.append("CVUT");
        // Generate 6 random digits
        for (int i = 0; i < 6; i++) {
            iban.append(random.nextInt(10));
        }
        // Generate 10 random digits for the CZ account number
        for (int i = 0; i < 10; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }

    public String generateCvv() {
        Random random = new Random();
        StringBuilder cvv = new StringBuilder();
        // Generate 3 random digits for the CVV code
        for (int i = 0; i < 3; i++) {
            cvv.append(random.nextInt(10));
        }
        return cvv.toString();
    }

    public String generatePin() {
        Random random = new Random();
        StringBuilder pin = new StringBuilder();
        // Generate 4 random digits for the PIN code
        for (int i = 0; i < 4; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }

    /**
     * Generates a SWIFT code with a specific format.
     * The format is "CVUTCZ" followed by 2 random uppercase letters.
     *
     * @return The generated SWIFT code.
     */
    public String generateSwift() {
        Random random = new Random();
        StringBuilder swift = new StringBuilder("CVUTCZ");
        for (int i = 0; i < 2; i++) {
            char randomChar = (char) (random.nextInt(26) + 'A');
            swift.append(randomChar);
        }
        return swift.toString();
    }

    /**
     * Generates an account number with a specific format.
     * The format is 10 random digits followed by "/0800".
     *
     * @return The generated account number.
     */
    public String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        // Generate 10 random digits for the account number
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

    public String generateCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder();
        // Generate 16 random digits for the card number
        for (int i = 0; i < 16; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    protected boolean countryExists(String countryName) {
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

    public static LocalDate getRandomStartDate() {
        int currentYear = LocalDate.now().getYear();

        int startYear = currentYear - 3;

        long startEpochDay = LocalDate.of(startYear, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(currentYear, 12, 31).toEpochDay();

        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay);
    }

    public static LocalDate getRandomExpirationDate(LocalDate startDate) {
        LocalDate minExpirationDate = startDate.plusYears(3); // 3 years from the start date
        LocalDate maxExpirationDate = startDate.plusYears(5); // 5 years from the start date

        // Convert these dates to epoch days
        long startEpochDay = minExpirationDate.toEpochDay();
        long endEpochDay = maxExpirationDate.toEpochDay();

        // Generate a random expiration date within this range
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay);
    }
}
