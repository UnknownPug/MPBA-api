package api.mpba.rastvdmy.service.generator;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A utility class for generating various financial data, such as bank numbers,
 * IBANs, CVVs, and more.
 */
@Component
public class FinancialDataGenerator {

    /**
     * Generates a random 4-digit bank number.
     *
     * @return a String representing a 4-digit bank number
     */
    public String generateBankNumber() {
        return generateRandomDigits(4);
    }

    /**
     * Generates a random IBAN, which consists of a country code, a check digit,
     * a bank identifier, and a random account number.
     *
     * @return a String representing a randomly generated IBAN
     */
    public String generateIban() {
        return "CZ" + generateRandomDigits(2)
                + "CVUT" + generateRandomDigits(6) + generateRandomDigits(10);
    }

    /**
     * Generates a random 3-digit CVV.
     *
     * @return a String representing a 3-digit CVV
     */
    public String generateCvv() {
        return generateRandomDigits(3);
    }

    /**
     * Generates a random 4-digit PIN.
     *
     * @return a String representing a 4-digit PIN
     */
    public String generatePin() {
        return generateRandomDigits(4);
    }

    /**
     * Generates a random SWIFT code consisting of eight uppercase letters.
     *
     * @return a String representing a random SWIFT code
     */
    public String generateSwift() {
        return generateRandomUppercaseLetters();
    }

    /**
     * Generates a random 10-digit account number.
     *
     * @return a String representing a 10-digit account number
     */
    public String generateAccountNumber() {
        return generateRandomDigits(10);
    }

    /**
     * Generates a random 16-digit card number.
     *
     * @return a String representing a 16-digit card number
     */
    public String generateCardNumber() {
        return generateRandomDigits(16);
    }

    /**
     * Generates a random start date within the last 3 years.
     *
     * @return a LocalDate representing a random start date
     */
    public static LocalDate getRandomStartDate() {
        int currentYear = LocalDate.now().getYear();
        int startYear = currentYear - 3;
        long startEpochDay = LocalDate.of(startYear, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(currentYear, 12, 31).toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay);
    }

    /**
     * Generates a random expiration date for a financial product, ensuring that
     * the expiration date is at least 2 years after the given start date and
     * at most 5 years after the start date.
     *
     * @param startDate the starting date from which the expiration date will be calculated
     * @return a LocalDate representing a random expiration date
     */
    public static LocalDate getRandomExpirationDate(LocalDate startDate) {
        LocalDate minExpirationDate = startDate.plusYears(2);
        LocalDate maxExpirationDate = startDate.plusYears(5);

        int randomYear = ThreadLocalRandom.current().nextInt(
                minExpirationDate.getYear(), maxExpirationDate.getYear() + 1);

        return LocalDate.of(randomYear, startDate.getMonth(), 1)
                .with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Generates a string of random digits of the specified length.
     *
     * @param length the number of digits to generate
     * @return a String representing the generated random digits
     */
    private String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder digits = new StringBuilder();
        digits.append(random.nextInt(9) + 1); // Ensure the first digit is between 1 and 9
        for (int i = 1; i < length; i++) {
            digits.append(random.nextInt(10));
        }
        return digits.toString();
    }

    /**
     * Generates a string of eight random uppercase letters for a SWIFT code.
     *
     * @return a String representing the generated random uppercase letters
     */
    private String generateRandomUppercaseLetters() {
        Random random = new Random();
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 8; i++) { // Generate 8 random uppercase letters for SWIFT
            char randomChar = (char) (random.nextInt(26) + 'A');
            letters.append(randomChar);
        }
        return letters.toString();
    }
}
