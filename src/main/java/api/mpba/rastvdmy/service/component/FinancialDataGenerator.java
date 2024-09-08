package api.mpba.rastvdmy.service.component;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class FinancialDataGenerator {

    public String generateBankNumber() {
        return generateRandomDigits(4);
    }

    public String generateIban() {
        return "CZ" + generateRandomDigits(2)
                + "CVUT" + generateRandomDigits(6) + generateRandomDigits(10);
    }

    public String generateCvv() {
        return generateRandomDigits(3);
    }

    public String generatePin() {
        return generateRandomDigits(4);
    }

    public String generateSwift() {
        return "CVUTCZ" + generateRandomUppercaseLetters();
    }

    public String generateAccountNumber() {
        return generateRandomDigits(10);
    }

    public String generateCardNumber() {
        return generateRandomDigits(16);
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
        LocalDate minExpirationDate = startDate.plusYears(3);
        LocalDate maxExpirationDate = startDate.plusYears(5);
        long startEpochDay = minExpirationDate.toEpochDay();
        long endEpochDay = maxExpirationDate.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay + 1);
        return LocalDate.ofEpochDay(randomDay);
    }

    private String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < length; i++) {
            digits.append(random.nextInt(10));
        }
        return digits.toString();
    }

    private String generateRandomUppercaseLetters() {
        Random random = new Random();
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            char randomChar = (char) (random.nextInt(26) + 'A');
            letters.append(randomChar);
        }
        return letters.toString();
    }
}