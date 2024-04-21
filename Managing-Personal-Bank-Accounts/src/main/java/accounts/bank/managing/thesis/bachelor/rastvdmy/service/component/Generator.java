package accounts.bank.managing.thesis.bachelor.rastvdmy.service.component;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * This class is responsible for generating various types of random strings.
 * It is annotated with @Component to indicate that it's a Spring managed bean.
 */
@Component
public class Generator {

    /**
     * Generates a reference number consisting of 8 random uppercase letters followed by 1 to 3 random digits.
     *
     * @return The generated reference number.
     */
    public String generateReferenceNumber() {
        String referenceNumber;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        // Generate 8 random uppercase letters
        for (int i = 0; i < 8; i++) {
            char randomChar = (char) (random.nextInt(26) + 'A');
            sb.append(randomChar);
        }

        // Determine the number of digits to append
        int numDigits = random.nextInt(3) + 1;

        // Generate the determined number of random digits
        for (int i = 0; i < numDigits; i++) {
            int randomDigit = random.nextInt(10);
            sb.append(randomDigit);
        }
        referenceNumber = sb.toString();
        return referenceNumber;
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
        accountNumber.append("/0800");
        return accountNumber.toString();
    }
}
