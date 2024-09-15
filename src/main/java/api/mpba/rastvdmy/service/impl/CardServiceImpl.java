package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.entity.BankAccount;
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

    public List<Card> getAccountCards(UUID id, HttpServletRequest request) {
        validateBankIdentityFromToken(request);
        return cardRepository.findAllByBankAccountId(id);
    }

    public Card getAccountCardById(UUID accountId, UUID cardId, HttpServletRequest request) {
        validateBankIdentityFromToken(request);

        return cardRepository.findByAccountIdAndId(accountId, cardId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified card not found."));
    }

    @Transactional
    public Card addAccountCard(UUID id, HttpServletRequest request, CardRequest cardRequest) throws Exception {
        validateBankIdentityFromToken(request);
        BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified bank account not found."));
        return generateCard(cardRequest.cardNumber(), cardRequest.cvv(), cardRequest.pin(), account);
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

        String encryptedCardNumber = EncryptionUtil.encrypt(cardNumber, secretKey, EncryptionUtil.generateIv());

        String encryptedCvv = EncryptionUtil.encrypt(cvv, secretKey, EncryptionUtil.generateIv());

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

    public void updateAccountCardStatus(UUID accountId, UUID cardId, HttpServletRequest request) {
        validateBankIdentityFromToken(request);
        Card card = cardRepository.findByAccountIdAndId(accountId, cardId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified card not found."));
        switch (card.getStatus()) {
            case STATUS_CARD_DEFAULT, STATUS_CARD_UNBLOCKED -> {
                card.setStatus(STATUS_CARD_BLOCKED);
                cardRepository.save(card);
            }
            case STATUS_CARD_BLOCKED -> {
                card.setStatus(STATUS_CARD_UNBLOCKED);
                cardRepository.save(card);
            }
        }
    }

    public void removeAccountCard(UUID accountId, UUID cardId, HttpServletRequest request) {
        validateBankIdentityFromToken(request);
        Card card = cardRepository.findByAccountIdAndId(accountId, cardId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified card not found."));
        cardRepository.delete(card);
    }

    private void validateBankIdentityFromToken(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String userEmail = jwtService.extractSubject(token);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));
        bankIdentityRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified bank identity not found."));
    }
}
