package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Generator {

    public String generateReferenceNumber() {
        Random random = new Random();
        StringBuilder referenceNumber = new StringBuilder();

        // Generate 8 random uppercase letters
        for (int i = 0; i < 8; i++) {
            char randomChar = (char) (random.nextInt(26) + 'A');
            referenceNumber.append(randomChar);
        }

        // Determine the number of digits to append
        int numDigits = random.nextInt(3) + 1;

        // Generate the determined number of random digits
        for (int i = 0; i < numDigits; i++) {
            int randomDigit = random.nextInt(10);
            referenceNumber.append(randomDigit);
        }

        return referenceNumber.toString();
    }

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
        // Generate 8 random digits for the CZ account number
        for (int i = 0; i < 8; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }

    public String generateSwift() {
        Random random = new Random();
        StringBuilder swift = new StringBuilder("CVUTCZ");
        // Generate 6 random uppercase letters for the location code
        for (int i = 0; i < 6; i++) {
            char randomChar = (char) (random.nextInt(26) + 'A');
            swift.append(randomChar);
        }
        return swift.toString();
    }
}
