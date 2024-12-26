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
import java.util.UUID;

/**
 * Implementation of the PaymentService interface for managing payment operations,
 * including bank transfers and card payments.
 * This service handles the creation,
 * retrieval, and validation of payments while ensuring proper encryption and security.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BankAccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CurrencyDataService currencyDataService;
    private final JwtService jwtService;
    private final UserProfileRepository userProfileRepository;

    /**
     * Constructor for PaymentServiceImpl.
     *
     * @param paymentRepository     the payment repository to be used
     * @param accountRepository     the bank account repository to be used
     * @param cardRepository        the card repository to be used
     * @param currencyDataService   the currency data service to be used
     * @param jwtService            the JWT service to be used
     * @param userProfileRepository the user profile repository to be used
     */
    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BankAccountRepository accountRepository,
                              CardRepository cardRepository,
                              CurrencyDataService currencyDataService,
                              JwtService jwtService, UserProfileRepository userProfileRepository) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.currencyDataService = currencyDataService;
        this.jwtService = jwtService;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Retrieves all payments associated with a given bank account or card.
     *
     * @param request   the HTTP request containing user data
     * @param bankName  the name of the bank associated with the payments
     * @param accountId the ID of the bank account
     * @return a list of payments related to the specified account
     * @throws ApplicationException if the bank account is invalid or no payments are found
     */
    public List<Payment> getAllPayments(HttpServletRequest request, String bankName, UUID accountId) {
        BankAccount bankAccount = getBankAccount(request, accountId);

        if (!bankAccount.getBankIdentity().getBankName().equalsIgnoreCase(bankName.trim())) {
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

    /**
     * Retrieves a specific payment by its ID.
     *
     * @param request   the HTTP request containing user data
     * @param bankName  the name of the bank associated with the payment
     * @param accountId the ID of the bank account
     * @param paymentId the ID of the payment to retrieve
     * @return the requested payment
     * @throws ApplicationException if the payment is not found or is not associated with the specified bank
     */
    public Payment getPaymentById(HttpServletRequest request, String bankName,
                                  UUID accountId, UUID paymentId) {
        BankAccount bankAccount = getBankAccount(request, accountId);

        if (!bankAccount.getBankIdentity().getBankName().equalsIgnoreCase(bankName.trim())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Payment is not connected to the specified bank.");
        }

        UUID cardId = paymentRepository.findCardIdByPaymentId(paymentId).orElse(null);

        Payment payment;
        if (cardId != null) {
            payment = paymentRepository.findBySenderCardIdAndId(cardId, paymentId)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Payment not found."));
        } else {
            payment = paymentRepository.findBySenderAccountIdAndId(accountId, paymentId)
                    .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Payment not found."));
        }

        decryptPaymentData(payment);
        return payment;
    }

    /**
     * Retrieves the bank account associated with the user's JWT token.
     *
     * @param request   the HTTP request containing user data
     * @param accountId the ID of the bank account to retrieve
     * @return the associated BankAccount
     * @throws ApplicationException if the user is not authorized, the user is blocked, or the account is not found.
     */
    private BankAccount getBankAccount(HttpServletRequest request, UUID accountId) {
        String token = jwtService.extractToken(request);
        String userEmail = jwtService.extractSubject(token);

        UserProfile userProfile = userProfileRepository.findByEmail(userEmail).orElseThrow(
                () -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User not authorized."));

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable. User is blocked.");
        }

        return accountRepository.findById(accountId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );
    }

    /**
     * Decrypts sensitive payment data to ensure secure handling.
     *
     * @param payment the payment object containing encrypted data
     * @throws ApplicationException if decryption fails
     */
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

    /**
     * Creates a bank transfer payment.
     *
     * @param request         the HTTP request containing user data
     * @param accountId       the ID of the bank account
     * @param recipientNumber the account number of the recipient
     * @param amount          the amount to be transferred
     * @param description     a description for the payment
     * @return the created Payment object
     * @throws Exception if an error occurs during payment creation
     */
    public Payment createBankTransfer(HttpServletRequest request, UUID accountId,
                                      String recipientNumber, BigDecimal amount, String description) throws Exception {
        BankAccount senderAccount = accountRepository.findById(accountId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );

        BankAccount recipientAccount = findBankAccountByNumber(recipientNumber);

        PaymentFactory paymentFactory = new BankTransferPaymentFactory();
        Payment payment = paymentFactory.createPayment(senderAccount, description, recipientAccount, null);

        validateAmount(amount);

        if (isAmountSufficient(senderAccount, amount, recipientAccount.getCurrency(), payment)) {
            processBankPayment(recipientAccount, payment);
        }
        return paymentRepository.save(payment);
    }

    /**
     * Finds a bank account by its account number.
     *
     * @param recipientNumber the account number of the recipient
     * @return the found BankAccount
     * @throws ApplicationException if no accounts are found or the recipient account is not found
     */
    private BankAccount findBankAccountByNumber(String recipientNumber) {
        List<BankAccount> accounts = accountRepository.findAll();

        if (accounts.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "There are no accounts.");
        }

        return accounts.stream()
                .filter(account -> isNumberValid(recipientNumber.trim(), account))
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Recipient account not found.")
                );
    }

    /**
     * Validates the recipient account number against the bank account.
     *
     * @param recipientNumber the account number to validate
     * @param bankAccount     the bank account to validate against
     * @return true if the numbers match, false otherwise
     */
    private static boolean isNumberValid(String recipientNumber, BankAccount bankAccount) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            return recipientNumber.trim().equals(EncryptionUtil.decrypt(bankAccount.getAccountNumber(), secretKey));
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    /**
     * Validates the amount to be transferred.
     *
     * @param amount the amount to be transferred
     * @throws ApplicationException if the amount is invalid
     */
    private void validateAmount(BigDecimal amount) {
        if (amount.equals(BigDecimal.ZERO)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }
    }

    /**
     * Processes a bank payment by adding the amount to the recipient's account.
     *
     * @param recipientAccount the bank account of the recipient
     * @param payment          the payment object to be processed
     */
    private void processBankPayment(BankAccount recipientAccount, Payment payment) {
        recipientAccount.setBalance(recipientAccount.getBalance().add(payment.getAmount()));
        accountRepository.save(recipientAccount);
        payment.setStatus(FinancialStatus.RECEIVED);
    }

    /**
     * Creates a card payment.
     *
     * @param request   the HTTP request containing user data
     * @param accountId the ID of the bank account
     * @param cardId    the ID of the card
     * @return the created Payment object
     * @throws Exception if an error occurs during payment creation
     */
    public Payment createCardPayment(HttpServletRequest request, UUID accountId, UUID cardId) throws Exception {
        BankAccount account = getBankAccount(request, accountId);

        Card card = findCardByIdAndAccountId(account.getId(), cardId);

        PaymentFactory paymentFactory = new CardPaymentFactory();
        Payment payment = paymentFactory.createPayment(account, null, null, card);

        if (isAmountSufficient(account, payment.getAmount(), payment.getCurrency(), payment)) {
            payment.setStatus(FinancialStatus.RECEIVED);
            paymentRepository.save(payment);
        }
        return paymentRepository.save(payment);
    }

    /**
     * Finds a card by its ID and associated bank account.
     *
     * @param accountId the ID of the bank account
     * @param cardId    the ID of the card
     * @return the found Card
     * @throws ApplicationException if the card is not found or is blocked
     */
    private Card findCardByIdAndAccountId(UUID accountId, UUID cardId) {
        Card card = cardRepository.findByAccountIdAndId(accountId, cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card not found.")
        );
        validateCardStatus(card);
        return card;
    }

    /**
     * Validates the status of a card before processing a payment.
     *
     * @param card the card to validate
     * @throws ApplicationException if the card is blocked or expired
     */
    private void validateCardStatus(Card card) {
        if (card.getStatus().equals(CardStatus.STATUS_CARD_BLOCKED)
                || card.getExpirationDate().isBefore(LocalDate.now())) {

            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Operation is unavailable, card is unavailable to use.");
        }
    }

    /**
     * Checks if the sender's account has sufficient balance for the payment.
     * If the balance is sufficient, the amount is subtracted from the sender's account.
     * If the currencies of the sender and recipient are different, the amount is converted.
     *
     * @param senderAccount the bank account of the sender
     * @param amount        the amount to be transferred
     * @param currency      the currency of the recipient
     * @param payment       the payment object to be updated
     * @return true if the amount is sufficient, false otherwise
     */
    private boolean isAmountSufficient(BankAccount senderAccount, BigDecimal amount,
                                       Currency currency, Payment payment) {

        if (senderAccount.getBalance().compareTo(amount) < 0) {
            payment.setStatus(FinancialStatus.DENIED);
            payment.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
            payment.setCurrency(currency);
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

    /**
     * Converts the amount to be transferred to the recipient's currency.
     *
     * @param amount           the amount to be transferred
     * @param senderCurrency   the currency of the sender
     * @param receiverCurrency the currency of the recipient
     * @param payment          the payment object to be updated
     */
    private void convertCurrency(BigDecimal amount, Currency senderCurrency,
                                 Currency receiverCurrency, Payment payment) {

        BigDecimal exchangeRate = currencyDataService.convertCurrency(
                senderCurrency.toString(), receiverCurrency.toString()).getRate();

        BigDecimal convertedAmount = amount.multiply(exchangeRate);

        payment.setAmount(convertedAmount.setScale(2, RoundingMode.HALF_UP));
        payment.setCurrency(receiverCurrency);
    }
}
