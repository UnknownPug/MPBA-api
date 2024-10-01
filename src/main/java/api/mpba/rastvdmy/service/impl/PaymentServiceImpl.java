package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.*;
import api.mpba.rastvdmy.entity.enums.*;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.*;
import api.mpba.rastvdmy.service.CurrencyDataService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static api.mpba.rastvdmy.entity.enums.Currency.getRandomCurrency;
import static api.mpba.rastvdmy.entity.enums.FinancialStatus.*;
import static api.mpba.rastvdmy.entity.enums.PaymentType.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final int MAX_PAYMENT = 6000;

    private final PaymentRepository paymentRepository;
    private final BankAccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CurrencyDataService currencyDataService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BankAccountRepository accountRepository,
                              CardRepository cardRepository,
                              CurrencyDataService currencyDataService,
                              JwtService jwtService, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.currencyDataService = currencyDataService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public List<Payment> getAllPayments(HttpServletRequest request, String bankName, UUID accountId) {
        BankAccount bankAccount = getBankAccount(request, accountId);

        if (!bankAccount.getBankIdentity().getBankName().equalsIgnoreCase(bankName)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Payments are not connected to the specified bank.");
        }

        List<Card> cards = cardRepository.findAllByAccountId(accountId).orElse(Collections.emptyList());
        List<UUID> cardsIds = cards.stream().map(Card::getId).toList();

        List<Payment> payments = paymentRepository.findAllBySenderAccountIdOrSenderCardId(accountId, cardsIds)
                .orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Payments not found."));
        payments.forEach(this::decryptPaymentData);

        return payments;
    }

    public Payment getPaymentById(HttpServletRequest request, String bankName,
                                  UUID accountId, UUID paymentId) {
        BankAccount bankAccount = getBankAccount(request, accountId);

        if (!bankAccount.getBankIdentity().getBankName().equalsIgnoreCase(bankName)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Payment is not connected to the specified bank.");
        }

        UUID cardId = paymentRepository.findCardIdByPaymentId(paymentId).orElse(null);

        Payment payment = paymentRepository.findBySenderAccountIdOrSenderCardIdAndId(accountId, cardId, paymentId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Payment not found."));

        decryptPaymentData(payment);
        return payment;
    }

    private BankAccount getBankAccount(HttpServletRequest request, UUID accountId) {
        String token = jwtService.extractToken(request);
        String userEmail = jwtService.extractSubject(token);

        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));

        if (user.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable. User is blocked.");
        }

        return accountRepository.findById(accountId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );
    }

    private void decryptPaymentData(Payment payment) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            payment.setSenderName(EncryptionUtil.decrypt(payment.getSenderName(), secretKey));
            payment.setRecipientName(EncryptionUtil.decrypt(payment.getRecipientName(), secretKey));
            if (payment.getDescription() != null) {
                payment.setDescription(EncryptionUtil.decrypt(payment.getDescription(), secretKey));
            }
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to decrypt payment data.");
        }
    }

    public Payment createBankTransfer(HttpServletRequest request, UUID accountId,
                                      String recipientNumber, BigDecimal amount, String description) throws Exception {

        BankAccount senderAccount = accountRepository.findById(accountId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );

        BankAccount recipientAccount = findBankAccountByNumber(recipientNumber);

        Payment payment = initializeBankPayment(senderAccount, description, recipientAccount);

        validateAmount(amount);

        if (isAmountSufficient(senderAccount, amount, recipientAccount.getCurrency(), payment)) {
            processBankPayment(recipientAccount, payment);
        }
        return paymentRepository.save(payment);
    }

    private BankAccount findBankAccountByNumber(String recipientNumber) {
        List<BankAccount> accounts = accountRepository.findAll();

        if (accounts.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "There are no accounts.");
        }

        return accounts.stream()
                .filter(account -> isNumberValid(recipientNumber, account))
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Recipient account not found.")
                );
    }

    private static boolean isNumberValid(String recipientNumber, BankAccount bankAccount) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            return recipientNumber.equals(EncryptionUtil.decrypt(bankAccount.getAccountNumber(), secretKey));
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    private Payment initializeBankPayment(BankAccount senderAccount, String description,
                                          BankAccount recipientAccount) throws Exception {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encryptedSenderName = EncryptionUtil.encrypt(
                senderAccount.getBankIdentity().getUser().getName() + " "
                        + senderAccount.getBankIdentity().getUser().getSurname(), secretKey);
        String encryptedRecipientName = EncryptionUtil.encrypt(
                recipientAccount.getBankIdentity().getUser().getName() + " "
                        + recipientAccount.getBankIdentity().getUser().getSurname(), secretKey);
        String encryptedDescription = EncryptionUtil.encrypt(description, secretKey);

        return Payment.builder()
                .id(UUID.randomUUID())
                .senderName(encryptedSenderName)
                .recipientName(encryptedRecipientName)
                .dateTime(LocalDate.now())
                .description(encryptedDescription)
                .type(BANK_TRANSFER)
                .senderAccount(senderAccount)
                .recipientAccount(recipientAccount)
                .build();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.equals(BigDecimal.ZERO)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }
    }

    private boolean isAmountSufficient(BankAccount senderAccount, BigDecimal amount,
                                       Currency currency, Payment payment) {

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            payment.setStatus(DENIED);
            return false;
        }
        // Subtract amount from sender account balance
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        accountRepository.save(senderAccount);

        if (!senderAccount.getCurrency().equals(currency)) {
            convertCurrency(amount, senderAccount.getCurrency(), currency, payment);
        } else {
            payment.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
            payment.setCurrency(currency);
        }
        return true;
    }

    private void convertCurrency(BigDecimal amount, Currency senderCurrency,
                                 Currency receiverCurrency, Payment payment) {
        BigDecimal exchangeRate = currencyDataService.convertCurrency(
                senderCurrency.toString(), receiverCurrency.toString()).getRate();
        BigDecimal convertedAmount = amount.multiply(exchangeRate);
        payment.setAmount(convertedAmount.setScale(2, RoundingMode.HALF_UP));
        payment.setCurrency(receiverCurrency);
    }

    private void processBankPayment(BankAccount recipientAccount, Payment payment) {
        recipientAccount.setBalance(recipientAccount.getBalance().add(payment.getAmount()));
        accountRepository.save(recipientAccount);
        payment.setStatus(RECEIVED);
    }

    public Payment createCardPayment(HttpServletRequest request, UUID accountId, UUID cardId) throws Exception {
        BankAccount account = getBankAccount(request, accountId);

        Card card = findCardByIdAndAccountId(account.getId(), cardId);

        Payment payment = initializeCardPayment(account, card);

        if (isAmountSufficient(account, payment.getAmount(), payment.getCurrency(), payment)) {
            payment.setStatus(RECEIVED);
            paymentRepository.save(payment);
        }
        return paymentRepository.save(payment);
    }

    private Card findCardByIdAndAccountId(UUID accountId, UUID cardId) {
        Card card = cardRepository.findByAccountIdAndId(accountId, cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card not found.")
        );
        validateCardStatus(card);
        return card;
    }

    private void validateCardStatus(Card card) {
        if (card.getStatus().equals(CardStatus.STATUS_CARD_BLOCKED)
                || card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Operation is unavailable, card is unavailable to use.");
        }
    }

    private Payment initializeCardPayment(BankAccount senderAccount, Card card) throws Exception {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encryptedSenderName = EncryptionUtil.encrypt(
                senderAccount.getBankIdentity().getUser().getName() + " "
                        + senderAccount.getBankIdentity().getUser().getSurname(), secretKey);
        String encryptedRecipientName = EncryptionUtil.encrypt(PurchaseCategory.getRandomCategory(), secretKey);

        return Payment.builder()
                .id(UUID.randomUUID())
                .senderName(encryptedSenderName)
                .recipientName(encryptedRecipientName)
                .dateTime(LocalDate.now())
                .amount(generateRandomAmount())
                .type(CARD_PAYMENT)
                .currency(getRandomCurrency())
                .senderCard(card)
                .build();
    }

    private BigDecimal generateRandomAmount() {
        Random randomAmount = new Random();
        return BigDecimal.valueOf(randomAmount.nextDouble(MAX_PAYMENT) + 1);
    }
}
