package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.CardRepository;
import api.mpba.rastvdmy.service.CardService;
import api.mpba.rastvdmy.service.TokenVerifierService;
import api.mpba.rastvdmy.service.generator.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

import static api.mpba.rastvdmy.entity.enums.CardStatus.*;
import static api.mpba.rastvdmy.entity.enums.CardType.getRandomCardType;

/**
 * Implementation of the CardService interface for managing user bank cards.
 * This service provides methods for adding, retrieving, and removing cards associated with bank accounts.
 * It includes functionality for card encryption and data masking to ensure sensitive information is handled securely.
 */
@Service
public class CardServiceImpl extends FinancialDataGenerator implements CardService {
    /**
     * Maximum number of cards that can be created per user.
     */
    public static final int MAX_AVAILABLE_CARDS = 3;

    /**
     * Minimum number of cards that can be created per user.
     */
    public static final int MIN_AVAILABLE_CARDS = 1;

    private final CardRepository cardRepository;
    private final BankIdentityRepository bankIdentityRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TokenVerifierService tokenVerifierService;

    /**
     * Constructs a CardServiceImpl with the specified repositories and services.
     *
     * @param cardRepository         the repository for card data
     * @param bankIdentityRepository the repository for bank identity data
     * @param bankAccountRepository  the repository for bank account data
     * @param tokenVerifierService  the service for extracting user token and getting user data from the request
     */
    @Autowired
    public CardServiceImpl(CardRepository cardRepository,
                           BankIdentityRepository bankIdentityRepository, BankAccountRepository bankAccountRepository,
                           TokenVerifierService tokenVerifierService) {
        this.cardRepository = cardRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.tokenVerifierService = tokenVerifierService;
    }

    /**
     * Retrieves all cards associated with a specific bank account.
     *
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account
     * @param request   the HTTP request containing user data
     * @return a list of cards associated with the specified account
     * @throws ApplicationException if no cards are found for the specified account
     */
    public List<Card> getAccountCards(String bankName, UUID accountId, HttpServletRequest request) {
        BankAccount account = getBankAccount(bankName, accountId, request);

        List<Card> cards = cardRepository.findAllByAccountId(account.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No cards found for specified account.")
        );
        return cards.stream().filter(card -> decryptCardData(card, false)).toList();
    }

    /**
     * Retrieves a specific card associated with a bank account by card ID.
     *
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account
     * @param cardId    the ID of the card to retrieve
     * @param request   the HTTP request containing user data
     * @param type      the visibility type (e.g., "visible" or "hidden")
     * @return the specified card if found
     * @throws ApplicationException if the specified card is not found
     */
    public Card getAccountCardById(String bankName, UUID accountId, UUID cardId,
                                   HttpServletRequest request, String type) {
        BankAccount account = getBankAccount(bankName, accountId, request);

        List<Card> cards = cardRepository.findAllByAccountId(account.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No cards found for specified account.")
        );

        boolean unmask = type.trim().equals("visible");
        return cards.stream()
                .filter(card -> card.getId().equals(cardId))
                .filter(card -> decryptCardData(card, unmask))
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified card not found.")
                );
    }

    /**
     * Decrypts the sensitive data of a card, optionally unmasking it.
     *
     * @param card   the card to decrypt
     * @param unmask whether to unmask the data
     * @return true if decryption was successful; false otherwise
     */
    public boolean decryptCardData(Card card, boolean unmask) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            String decryptedCardNumber = EncryptionUtil.decrypt(card.getCardNumber(), secretKey);
            String decryptedCVV = EncryptionUtil.decrypt(card.getCvv(), secretKey);

            if (unmask) {
                card.setCardNumber(decryptedCardNumber);
                card.setCvv(decryptedCVV);
            } else {
                card.setCardNumber(maskCardData(decryptedCardNumber));
                card.setCvv("*".repeat(decryptedCVV.length()));
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Masks the card data, leaving only the last four digits visible.
     *
     * @param data the card data to mask
     * @return the masked card data
     */
    private String maskCardData(String data) {
        int length = data.length();
        if (length <= 4) {
            return data;
        }
        return "*".repeat(length - 4) + data.substring(length - 4);
    }

    /**
     * Adds a new card associated with the specified bank account.
     *
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account
     * @param request   the HTTP request containing user data
     * @return the created Card
     * @throws Exception if there is an error while adding the card
     */
    @Transactional
    public Card addAccountCard(String bankName, UUID accountId, HttpServletRequest request) throws Exception {
        BankAccount account = getBankAccount(bankName, accountId, request);
        return generateCard(generateCardNumber(), generateCvv(), generatePin(), account);
    }

    /**
     * Connects a number of cards to a specified bank account.
     *
     * @param account the bank account to connect cards to
     * @throws Exception if there is an error while connecting cards
     */
    @Transactional
    public void connectCards(BankAccount account) throws Exception {
        List<CardType> cardTypes = new ArrayList<>(Arrays.asList(CardType.values()));
        List<CardStatus> cardStatuses = new ArrayList<>(Arrays.asList(CardStatus.values()));

        Collections.shuffle(cardTypes);
        Collections.shuffle(cardStatuses);

        Random generateRandom = new Random();
        int numberOfCards = generateRandom.nextInt(
                MAX_AVAILABLE_CARDS - MIN_AVAILABLE_CARDS) + MIN_AVAILABLE_CARDS;

        for (int i = 0; i < numberOfCards; i++) {
            generateCard(generateCardNumber(), generateCvv(), generatePin(), account);
        }
    }

    /**
     * Generates and encrypts a new card with the provided details.
     *
     * @param cardNumber the card number
     * @param cvv        the CVV of the card
     * @param pin        the PIN for the card
     * @param account    the bank account associated with the card
     * @return the generated Card
     * @throws Exception if there is an error while generating the card
     */
    @Transactional
    protected Card generateCard(String cardNumber, String cvv, String pin, BankAccount account) throws Exception {
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encryptedCardNumber = EncryptionUtil.encrypt(cardNumber, secretKey);
        String encryptedCvv = EncryptionUtil.encrypt(cvv, secretKey);
        String hashPin = EncryptionUtil.hash(pin);

        LocalDate startDate = getRandomStartDate();
        LocalDate expirationDate = getRandomExpirationDate(startDate);

        Card card = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber(encryptedCardNumber)
                .cvv(encryptedCvv)
                .pin(hashPin)
                .startDate(startDate)
                .expirationDate(expirationDate)
                .category(CardCategory.DEBIT)
                .type(getRandomCardType())
                .status(getRandomStatus())
                .account(account)
                .build();

        return cardRepository.save(card);
    }

    /**
     * Removes a specific card associated with a bank account.
     *
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account
     * @param cardId    the ID of the card to remove
     * @param request   the HTTP request containing user data
     * @throws ApplicationException if the specified card is not found
     */
    public void removeAccountCard(String bankName, UUID accountId, UUID cardId, HttpServletRequest request) {
        BankAccount account = getBankAccount(bankName, accountId, request);
        List<Card> cards = cardRepository.findAllByAccountId(account.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No cards found for specified account.")
        );

        Card card = cards.stream()
                .filter(cc -> cc.getId().equals(cardId))
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified card not found.")
                );

        cardRepository.delete(card);
    }

    /**
     * Removes all cards associated with a specified bank account.
     *
     * @param account the bank account whose cards should be removed
     * @throws ApplicationException if no cards are found for the specified account
     */
    @Transactional
    public void removeAllCards(BankAccount account) {
        List<Card> cards = cardRepository.findAllByAccountId(account.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No cards found for specified account.")
        );

        try {
            cardRepository.deleteAll(cards);
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while removing all cards.");
        }
    }

    /**
     * Retrieves the bank account associated with the specified bank name and account ID.
     *
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account
     * @param request   the HTTP request containing user data
     * @return the bank account associated with the specified bank name and account ID
     * @throws ApplicationException if the specified bank account or bank identity is not found
     */
    private BankAccount getBankAccount(String bankName, UUID accountId, HttpServletRequest request) {
        UserProfile userProfile = tokenVerifierService.getUserData(request);

        BankIdentity identity = bankIdentityRepository
                .findByUserProfileIdAndBankName(userProfile.getId(), bankName.trim())
                .orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank identity not found.")
                );

        List<BankAccount> bankAccounts = bankAccountRepository.findAllByBankIdentityId(identity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No accounts found for specified bank identity.")
        );

        return bankAccounts.stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst().orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND, "Specified bank account not found.")
                );
    }
}
