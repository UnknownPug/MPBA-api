package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * This class is responsible for managing cards.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses CardRepository and UserRepository to interact with the database.
 * It also uses a Generator to generate card numbers, IBANs, and SWIFT codes.
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CurrencyDataService currencyDataService;

    private final Generator generator;

    /**
     * Constructs a new CardService with the given repositories and generator.
     *
     * @param cardRepository      The CardRepository to use.
     * @param userRepository      The UserRepository to use.
     * @param currencyDataService The CurrencyDataService to use.
     * @param generator           The Generator to use.
     */
    @Autowired
    public CardService(CardRepository cardRepository, UserRepository userRepository,
                       CurrencyDataService currencyDataService, Generator generator) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.currencyDataService = currencyDataService;
        this.generator = generator;
    }

    /**
     * Retrieves all cards.
     *
     * @return A list of all cards.
     */
    @Cacheable(value = "cards")
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    /**
     * Retrieves cards with filtering and sorting.
     *
     * @param pageable The pagination information.
     * @return A page of cards.
     */
    @Cacheable(value = "cards")
    public Page<Card> filterAndSortCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    /**
     * Retrieves a card by its ID.
     *
     * @param cardId The ID of the card to retrieve.
     * @return The retrieved card.
     */
    @Cacheable(value = "cards", key = "#cardId")
    public Card getCardById(Long cardId) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card with id: " + cardId + " not found.")
        );
    }

    /**
     * Retrieves a card by its card number.
     *
     * @param cardNumber The card number of the card to retrieve.
     * @return The retrieved card.
     */
    @Cacheable(value = "cards", key = "#cardNumber")
    public Card getCardByCardNumber(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card with number: " + cardNumber + " not found.");
        }
        return cardRepository.findByCardNumber(cardNumber);
    }

    /**
     * Creates a new card for a user.
     *
     * @param userId         The ID of the user.
     * @param chosenCurrency The currency of the card.
     * @param type           The type of the card.
     * @return The created card.
     */
    @CacheEvict(value = {"cards", "users"}, allEntries = true)
    public Card createCard(Long userId, String chosenCurrency, String type) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Creating card is unavailable for blocked user.");
        }
        long minCardLimit = 1_000_000_000_000_000L;
        long maxCardLimit = 9_999_999_999_999_999L;
        int minCvvLimit = 100;
        int maxCvvLimit = 999;
        int minPinLimit = 1000;
        int maxPinLimit = 9999;

        Card card = new Card();
        Random random = new Random();

        long generatedCardNumber = minCardLimit + ((long) (random.nextDouble() * (maxCardLimit - minCardLimit)));
        String cardNumber = String.valueOf(generatedCardNumber);
        if (!isValidCardNumber(cardNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card number must be 16 digits.");
        }
        card.setCardNumber(HtmlUtils.htmlEscape(cardNumber));

        int generateCvv = random.nextInt(maxCvvLimit - minCvvLimit + 1) + minCvvLimit;
        card.setCvv(generateCvv);

        int generatePin = random.nextInt(maxPinLimit - minPinLimit + 1) + minPinLimit;
        card.setPin(generatePin);

        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);
        card.setHolderName(HtmlUtils.htmlEscape(user.getName()) + " " + HtmlUtils.htmlEscape(user.getSurname()));
        String generatedIban = HtmlUtils.htmlEscape(generator.generateIban());
        if (!isValidIban(generatedIban)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid IBAN.");
        }
        card.setIban(generatedIban);
        String generatedSwift = HtmlUtils.htmlEscape(generator.generateSwift());
        if (!isValidSwift(generatedSwift)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid SWIFT.");
        }
        card.setSwift(generatedSwift);
        if (chosenCurrency.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency must be filled.");
        }
        Currency currencyType;
        try {
            currencyType = Currency.valueOf(chosenCurrency.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Currency " + chosenCurrency + " does not exist.");
        }
        String generatedAccountNumber = HtmlUtils.htmlEscape(generator.generateAccountNumber());
        if (!isValidAccountNumber(generatedAccountNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid account number.");
        }
        card.setAccountNumber(generatedAccountNumber);
        card.setCurrencyType(currencyType);
        cardTypeCheck(type, card);
        card.setCardExpirationDate(LocalDate.now().plusYears(5));
        return cardRepository.save(card);
    }

    /**
     * Checks if the provided IBAN code is valid.
     *
     * @param ibanCode The IBAN code to check.
     * @return True if the IBAN code is not null and its length is 24, false otherwise.
     */
    private boolean isValidIban(String ibanCode) {
        return ibanCode != null && ibanCode.length() == 24;
    }

    /**
     * Checks if the provided card number is valid.
     *
     * @param cardNumber The card number to check.
     * @return True if the card number is not null and its length is 16, false otherwise.
     */
    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.length() == 16;
    }

    /**
     * Checks if the provided SWIFT code is valid.
     *
     * @param swift The SWIFT code to check.
     * @return True if the SWIFT code is not null, and its length is 8, false otherwise.
     */
    private boolean isValidSwift(String swift) {
        return swift != null && swift.length() == 8;
    }

    /**
     * Checks if the provided account number is valid.
     *
     * @param accountNumber The account number to check.
     * @return True if the account number is not null and matches the pattern of 10 digits
     * followed by a forward slash and 4 digits, false otherwise.
     */
    private boolean isValidAccountNumber(String accountNumber) {
        // Regular expression to match 10 digits followed by a forward slash and 4 digits
        String regex = "^\\d{10}/\\d{4}$";
        return accountNumber != null && accountNumber.matches(regex);
    }

    /**
     * Checks if the provided card type is valid and sets it to the card.
     *
     * @param type The card type to check.
     * @param card The card to set the type to.
     * @throws ApplicationException if the card type is empty or does not exist.
     */
    private void cardTypeCheck(String type, Card card) {
        if (type.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card type must be filled.");
        }
        CardType cardType;
        try {
            cardType = CardType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card type " + type + " does not exist.");
        }
        card.setCardType(cardType);
    }

    /**
     * Refills a card.
     *
     * @param cardId  The ID of the card to refill.
     * @param pin     The pin of the card.
     * @param balance The amount to refill.
     */
    @CacheEvict(value = "cards", allEntries = true)
    public void cardRefill(Long cardId, Integer pin, BigDecimal balance) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found.")
        );
        if (card.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable for blocked card.");
        }
        if (checkCardExpirationDate(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card is expired.");
        }
        if (card.getPin().equals(pin) && (card.getStatus().equals(CardStatus.STATUS_CARD_UNBLOCKED) ||
                card.getStatus().equals(CardStatus.STATUS_CARD_DEFAULT))) {
            if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Money amount must be greater than 0.");
            }
            conversationToCardCurrency(card, balance);
            card.setRecipientTime(LocalDateTime.now());
            cardRepository.save(card);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid pin or card is blocked.");
        }
    }

    /**
     * Converts the provided balance to the currency of the card and adds it to the card's balance.
     * The conversion rate is retrieved from the CurrencyDataService.
     *
     * @param card    The card to which the balance will be added.
     * @param balance The balance to add to the card. This balance is in a different currency and will be converted to the card's currency.
     */
    private void conversationToCardCurrency(Card card, BigDecimal balance) {
        switch (card.getCurrencyType()) {
            case USD -> card.setBalance(card.getBalance().add(balance).multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.USD.toString()).getRate())
            ));
            case EUR -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.EUR.toString()).getRate()))
            ));
            case UAH -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.UAH.toString()).getRate()))
            ));
            case CZK -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.CZK.toString()).getRate()))
            ));
            case PLN -> card.setBalance(card.getBalance().add(balance.multiply(
                    BigDecimal.valueOf(currencyDataService.findByCurrency(Currency.PLN.toString()).getRate()))
            ));
        }
    }

    /**
     * Updates the status of a card.
     *
     * @param id The ID of the card to update.
     */
    @CacheEvict(value = "cards", allEntries = true)
    public void updateCardStatus(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card with id: " + id + " not found.")
        );
        if (checkCardExpirationDate(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card is expired.");
        }
        switch (card.getStatus()) {
            case STATUS_CARD_BLOCKED -> {
                card.setStatus(CardStatus.STATUS_CARD_UNBLOCKED);
                cardRepository.save(card);
            }
            case STATUS_CARD_DEFAULT, STATUS_CARD_UNBLOCKED -> {
                card.setStatus(CardStatus.STATUS_CARD_BLOCKED);
                cardRepository.save(card);
            }
        }
    }

    /**
     * Changes the type of card.
     *
     * @param cardId The ID of the card to change.
     */
    @CacheEvict(value = "cards", allEntries = true)
    public void changeCardType(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found.")
        );
        if (checkCardExpirationDate(card)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card is expired.");
        }
        if (card.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable for blocked card.");
        }
        switch (card.getCardType()) {
            case VISA -> {
                card.setCardType(CardType.MASTERCARD);
                cardRepository.save(card);
            }
            case MASTERCARD -> {
                card.setCardType(CardType.VISA);
                cardRepository.save(card);
            }
        }
    }

    /**
     * Deletes a card.
     *
     * @param cardId The ID of the card to delete.
     * @param userId The ID of the user.
     */
    @Transactional
    @CacheEvict(value = {"cards", "users"}, allEntries = true)
    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "Card with id: " + cardId + " not found.")
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NO_CONTENT, "User with id: " + userId + " not found.")
        );
        if (card.getCardLoan() == null || card.getCardLoan().getLoanAmount().compareTo(BigDecimal.ZERO) == 0) {
            if (card.getDepositTransaction() == null
                    || card.getDepositTransaction().getDepositAmount().compareTo(BigDecimal.ZERO) == 0) {
                if (card.getBalance().compareTo(BigDecimal.ZERO) == 0 && user.getCards().contains(card)) {
                    user.getCards().remove(card);
                    userRepository.save(user);
                    cardRepository.delete(card);
                } else {
                    throw new ApplicationException(HttpStatus.BAD_REQUEST,
                            "Card is not empty or user does not contain this card.");
                }
            } else {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card has deposit transaction.");
            }
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Card has loan.");
        }
    }

    /**
     * Checks if the card's expiration date has passed.
     *
     * @param card The card to check.
     * @return True if the current date is after the card's expiration date, false otherwise.
     */
    private boolean checkCardExpirationDate(Card card) {
        return LocalDate.now().isAfter(card.getCardExpirationDate());
    }
}
