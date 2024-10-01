package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.CardRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.CardService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.utils.FinancialDataGenerator;
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

@Service
public class CardServiceImpl extends FinancialDataGenerator implements CardService {
    public static final int MAX_AVAILABLE_CARDS = 3;
    public static final int MIN_AVAILABLE_CARDS = 1;
    private final CardRepository cardRepository;
    private final BankIdentityRepository bankIdentityRepository;
    private final BankAccountRepository bankAccountRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository,
                           BankIdentityRepository bankIdentityRepository,
                           BankAccountRepository bankAccountRepository, JwtService jwtService, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public List<Card> getAccountCards(String bankName, UUID accountId, HttpServletRequest request) {
        BankAccount account = getBankAccount(bankName, accountId, request);

        List<Card> cards = cardRepository.findAllByAccountId(account.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No cards found for specified account.")
        );
        return cards.stream().filter(this::decryptCardData).toList();
    }

    public Card getAccountCardById(String bankName, UUID accountId, UUID cardId, HttpServletRequest request) {
        BankAccount account = getBankAccount(bankName, accountId, request);

        List<Card> cards = cardRepository.findAllByAccountId(account.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No cards found for specified account.")
        );

        return cards.stream()
                .filter(card -> card.getId().equals(cardId))
                .filter(this::decryptCardData)
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified card not found.")
                );
    }

    public boolean decryptCardData(Card card) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            card.setCardNumber(EncryptionUtil.decrypt(card.getCardNumber(), secretKey));
            card.setCvv(EncryptionUtil.decrypt(card.getCvv(), secretKey));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public Card addAccountCard(String bankName, UUID accountId, HttpServletRequest request) throws Exception {
        BankAccount account = getBankAccount(bankName, accountId, request);
        return generateCard(generateCardNumber(), generateCvv(), generatePin(), account);
    }

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

    private BankAccount getBankAccount(String bankName, UUID accountId, HttpServletRequest request) {
        User user = BankAccountServiceImpl.getUserData(request, jwtService, userRepository);

        BankIdentity identity = bankIdentityRepository.findByUserIdAndBankName(user.getId(), bankName).orElseThrow(
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
