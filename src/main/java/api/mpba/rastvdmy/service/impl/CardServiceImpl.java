package api.mpba.rastvdmy.service.impl;


import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.enums.CardCategory;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.CardType;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.CardRepository;
import api.mpba.rastvdmy.service.CardService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.component.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static api.mpba.rastvdmy.entity.enums.CardStatus.*;

@Service
public class CardServiceImpl extends FinancialDataGenerator implements CardService {
    public static final int MAX_AVAILABLE_CARDS = 3;
    public static final int MIN_AVAILABLE_CARDS = 1;
    private final CardRepository cardRepository;
    private final BankIdentityRepository bankIdentityRepository;
    private final BankAccountRepository bankAccountRepository;
    private final JwtService jwtService;

    @Autowired
    public CardServiceImpl(CardRepository cardRepository,
                           BankIdentityRepository bankIdentityRepository,
                           BankAccountRepository bankAccountRepository, JwtService jwtService) {
        this.cardRepository = cardRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.jwtService = jwtService;
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

    public Card addAccountCard(UUID id, HttpServletRequest request, CardRequest cardRequest) throws Exception {
        validateBankIdentityFromToken(request);
        BankAccount account = bankAccountRepository.findById(id.toString())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified bank account not found."));

        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encryptedCardNumber = EncryptionUtil.encrypt(cardRequest.cardNumber(), secretKey, EncryptionUtil.generateIv());
        String encryptedCvv = EncryptionUtil.encrypt(cardRequest.cvv(), secretKey, EncryptionUtil.generateIv());
        LocalDate encodedExpirationDate = LocalDate.parse(EncryptionUtil.encrypt(
                getRandomExpirationDate(getRandomStartDate()).toString(), secretKey, EncryptionUtil.generateIv()
        ));
        String hashPin = EncryptionUtil.hash(cardRequest.pin());

        Card card = Card.builder().
                category(CardCategory.DEBIT)
                .type(CardType.getRandomCardType())
                .status(CardStatus.getRandomStatus())
                .cardNumber(encryptedCardNumber)
                .cvv(encryptedCvv)
                .pin(hashPin)
                .startDate(getRandomStartDate())
                .expirationDate(encodedExpirationDate)
                .account(account)
                .build();
        return cardRepository.save(card);
    }

    public List<Card> generateCards(UUID accountId) {
        BankAccount account = bankAccountRepository.findById(accountId.toString())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified bank account not found."));

        List<CardType> cardTypes = new ArrayList<>(Arrays.asList(CardType.values()));
        Collections.shuffle(cardTypes);

        List<CardStatus> cardStatuses = new ArrayList<>(Arrays.asList(CardStatus.values()));
        Collections.shuffle(cardStatuses);

        List<Card> cards = new ArrayList<>();
        Random generateRandom = new Random();

        Card card = Card.builder()
                .category(CardCategory.DEBIT)
                .type(CardType.VISA)
                .status(STATUS_CARD_DEFAULT)
                .cardNumber(generateCardNumber())
                .cvv(generateCvv())
                .pin(generatePin())
                .startDate(getRandomStartDate())
                .expirationDate(getRandomExpirationDate(getRandomStartDate()))
                .account(account)
                .build();

        cards.add(card);

        int numberOfCards = generateRandom.nextInt(MAX_AVAILABLE_CARDS + MIN_AVAILABLE_CARDS);

        cards.addAll(
                IntStream.range(0, numberOfCards)
                        .mapToObj(i -> Card.builder()
                                .category(CardCategory.DEBIT)
                                .type(cardTypes.get(i))
                                .status(cardStatuses.remove(i))
                                .cardNumber(generateCardNumber())
                                .cvv(generateCvv())
                                .pin(generatePin())
                                .startDate(getRandomStartDate())
                                .expirationDate(getRandomExpirationDate(getRandomStartDate()))
                                .account(account)
                                .build()
                        ).toList()
        );
        return cardRepository.saveAll(cards);
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
        final String bankIdentityId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!
        bankIdentityRepository.findById(bankIdentityId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Specified bank identity not found."));
    }
}
