package api.mpba.rastvdmy.service.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
        return generateRandomUppercaseLetters();
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
        LocalDate minExpirationDate = startDate.plusYears(2);
        LocalDate maxExpirationDate = startDate.plusYears(5);

        int randomYear = ThreadLocalRandom.current().nextInt(
                minExpirationDate.getYear(), maxExpirationDate.getYear() + 1);

        return LocalDate.of(randomYear, startDate.getMonth(), 1)
                .with(TemporalAdjusters.lastDayOfMonth());
    }

    private String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder digits = new StringBuilder();
        digits.append(random.nextInt(9) + 1); // Ensure the first digit is between 1 and 9
        for (int i = 1; i < length; i++) {
            digits.append(random.nextInt(10));
        }
        return digits.toString();
    }

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