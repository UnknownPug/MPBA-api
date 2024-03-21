package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Autowired
    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found")
        );
    }

    public Card createCard(Long userId, String chosenCurrency, String type) {
        long minCardLimit = 1_000_000_000_000_000L;
        long maxCardLimit = 9_999_999_999_999_999L;
        int minCvvLimit = 100;
        int maxCvvLimit = 999;
        int minPinLimit = 1000;
        int maxPinLimit = 9999;

        Card card = new Card();
        Random random = new Random();

        long generatedCardNumber = minCardLimit + random.nextLong() * (maxCardLimit - minCardLimit);
        card.setCardNumber(String.valueOf(generatedCardNumber));

        int generateCvv = (minCvvLimit + random.nextInt() * (maxCvvLimit - minCvvLimit));
        card.setCvv(generateCvv);

        int generatePin = (minPinLimit + random.nextInt() * (maxPinLimit - minPinLimit));
        card.setPin(generatePin);

        card.setBalance(BigDecimal.ZERO);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "User with id: " + userId + " not found")
        );
        card.setHolderName(user.getName() + " " + user.getSurname());
        card.setIban(generateIban());
        card.setSwift(generateSwift());
        if (chosenCurrency.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency must be filled");
        }
        Currency currencyType;
        try {
            currencyType = Currency.valueOf(chosenCurrency.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency " + chosenCurrency + " does not exist");
        }
        card.setCurrencyType(currencyType);
        cardTypeCheck(type, card);
        card.setStatus(CardStatus.STATUS_CARD_UNBLOCKED);
        card.setCardExpirationDate(LocalDateTime.now().plusYears(5));
        return cardRepository.save(card);
    }

    private void cardTypeCheck(String type, Card card) {
        if (type.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card type must be filled");
        }
        CardType cardType;
        try {
            cardType = CardType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card type " + type + " does not exist");
        }
        card.setCardType(cardType);
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

    private String generateSwift() {
        Random random = new Random();
        StringBuilder swift = new StringBuilder("CVUTCZ");
        // Generate 6 random uppercase letters for the location code
        for (int i = 0; i < 6; i++) {
            char randomChar = (char) (random.nextInt(26) + 'A');
            swift.append(randomChar);
        }
        return swift.toString();
    }

    public void cardRefill(Long cardId, Integer pin, BigDecimal balance) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found")
        );
        if (card.getPin().equals(pin) && card.getStatus().equals(CardStatus.STATUS_CARD_UNBLOCKED)) {
            card.setBalance(card.getBalance().add(balance));
            card.setRecipientTime(LocalDateTime.now());
            cardRepository.save(card);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid pin or card is blocked");
        }
    }

    public void changeCardStatus(Long cardId, String cardStatus) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found")
        );
        if (cardStatus.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card status must be filled");
        }
        CardStatus status;
        try {
            status = CardStatus.valueOf(cardStatus.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card status " + cardStatus + " does not exist");
        }
        card.setStatus(status);
        cardRepository.save(card);
    }

    public void changeCardType(Long cardId, String cardType) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found")
        );
        cardTypeCheck(cardType, card);
        cardRepository.save(card);
    }

    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "User with id: " + userId + " not found")
        );
        if (card.getBalance().equals(BigDecimal.ZERO) && user.getCards().contains(card)) {
            cardRepository.deleteById(cardId);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Card is not empty or user does not contain this card");
        }
    }
}
