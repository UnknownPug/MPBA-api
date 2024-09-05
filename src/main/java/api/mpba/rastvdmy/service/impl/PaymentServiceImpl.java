package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.dto.request.PaymentRequest;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.Payment;
import api.mpba.rastvdmy.entity.enums.CardStatus;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.enums.FinancialStatus;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.CardRepository;
import api.mpba.rastvdmy.repository.PaymentRepository;
import api.mpba.rastvdmy.service.CurrencyDataService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.PaymentService;
import api.mpba.rastvdmy.service.component.PurchaseCategory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BankIdentityRepository identityRepository;
    private final BankAccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CurrencyDataService currencyDataService;
    private final JwtService jwtService;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BankIdentityRepository identityRepository,
                              BankAccountRepository accountRepository,
                              CardRepository cardRepository,
                              CurrencyDataService currencyDataService,
                              JwtService jwtService) {
        this.paymentRepository = paymentRepository;
        this.identityRepository = identityRepository;
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.currencyDataService = currencyDataService;
        this.jwtService = jwtService;
    }

    public List<Payment> getPayments(HttpServletRequest request) {
        BankIdentity identity = getBankIdentity(request);
        BankAccount account = accountRepository.findByBankIdentityId(identity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );
        return paymentRepository.findAllBySenderAccountId(account.getId());
    }

    public Page<Payment> filterAndSortTransfers(HttpServletRequest request, PageRequest pageable) {
        BankIdentity identity = getBankIdentity(request);
        BankAccount account = accountRepository.findByBankIdentityId(identity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );
        return paymentRepository.findAllBySenderAccountId(account.getId(), pageable);
    }

    public Payment getPaymentById(HttpServletRequest request, UUID paymentId) {
        BankIdentity identity = getBankIdentity(request);
        BankAccount account = accountRepository.findByBankIdentityId(identity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );
        return paymentRepository.findBySenderAccountId(account.getId());
    }

    public Payment createBankPayment(HttpServletRequest request, PaymentRequest paymentRequest,
                                     String recipientNumber) {
        BankIdentity identity = getBankIdentity(request);
        validateUserStatus(identity);

        BankAccount senderAccount = getBankAccountByIdentityId(identity.getId());
        BankAccount recipientAccount = getBankAccountByNumber(recipientNumber);

        Payment payment = initializePayment(paymentRequest, senderAccount, recipientAccount);

        validateSufficientFunds(senderAccount, paymentRequest.amount());

        processPayment(senderAccount, recipientAccount, paymentRequest.amount(), payment);

        return finalizePayment(senderAccount, recipientAccount, payment);
    }

    public Payment createCardPayment(HttpServletRequest request, PaymentRequest paymentRequest) {
        BankIdentity identity = getBankIdentity(request);
        validateUserStatus(identity);

        BankAccount senderAccount = getBankAccountByIdentityId(identity.getId());
        Card card = getCardByAccountId(senderAccount.getId());

        validateCardStatus(card);

        Payment payment = initializeCardPayment(paymentRequest, senderAccount, card);

        BigDecimal amount = generateRandomAmount();
        validateSufficientFunds(senderAccount, amount);

        processCardPayment(senderAccount, amount, payment);

        return finalizeCardPayment(senderAccount, payment, amount);
    }

    private void validateUserStatus(BankIdentity identity) {
        if (identity.getUser().getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable. User is blocked.");
        }
    }

    private BankAccount getBankAccountByIdentityId(UUID identityId) {
        return accountRepository.findByBankIdentityId(identityId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.")
        );
    }

    private BankAccount getBankAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Recipient account not found.")
        );
    }

    private Card getCardByAccountId(UUID accountId) {
        return cardRepository.findByAccountId(accountId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card not found.")
        );
    }

    private void validateCardStatus(Card card) {
        if (card.getStatus().equals(CardStatus.STATUS_CARD_BLOCKED) || card.getExpirationDate().isBefore(LocalDate.now())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable.");
        }
    }

    private Payment initializePayment(PaymentRequest paymentRequest, BankAccount senderAccount, BankAccount recipientAccount) {
        Payment payment = new Payment();
        payment.setType(paymentRequest.type());
        payment.setSenderName(senderAccount.getBankIdentity().getUser().getName() +
                " " + senderAccount.getBankIdentity().getUser().getSurname()
        );
        payment.setRecipientName(recipientAccount.getBankIdentity().getUser().getName() +
                " " + recipientAccount.getBankIdentity().getUser().getSurname()
        );
        payment.setDateTime(LocalDate.now());
        payment.setSenderAccount(senderAccount);
        payment.setRecipientAccount(recipientAccount);
        payment.setDescription(paymentRequest.description());
        return payment;
    }

    private Payment initializeCardPayment(PaymentRequest paymentRequest, BankAccount senderAccount, Card card) {
        Payment payment = new Payment();
        payment.setType(paymentRequest.type());
        payment.setSenderName(senderAccount.getBankIdentity().getUser().getName() +
                " " + senderAccount.getBankIdentity().getUser().getSurname()
        );
        payment.setRecipientName(PurchaseCategory.getRandomCategory());
        payment.setDateTime(LocalDate.now());
        payment.setDescription(paymentRequest.description());
        payment.setSenderCard(card);
        payment.setCurrency(Currency.getRandomCurrency());
        return payment;
    }

    private void validateSufficientFunds(BankAccount senderAccount, BigDecimal amount) {
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Insufficient funds.");
        }
    }

    private void processPayment(BankAccount senderAccount, BankAccount recipientAccount, BigDecimal amount, Payment payment) {
        if (senderAccount.getCurrency().equals(recipientAccount.getCurrency())) {
            payment.setAmount(amount);
            payment.setCurrency(senderAccount.getCurrency());
            senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
            recipientAccount.setBalance(recipientAccount.getBalance().add(amount));
        } else if (senderAccount.getBankIdentity().getUser().equals(recipientAccount.getBankIdentity().getUser())) {
            transferCurrency(amount, senderAccount.getCurrency(), recipientAccount.getCurrency(), payment);
            payment.setCurrency(recipientAccount.getCurrency());
            senderAccount.setBalance(senderAccount.getBalance().subtract(payment.getAmount()));
            recipientAccount.setBalance(recipientAccount.getBalance().add(payment.getAmount()));
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Different currency types ...");
        }
    }

    private void processCardPayment(BankAccount senderAccount, BigDecimal amount, Payment payment) {
        if (senderAccount.getCurrency().equals(payment.getCurrency())) {
            payment.setAmount(amount);
            senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        } else {
            transferCurrency(amount, senderAccount.getCurrency(), payment.getCurrency(), payment);
            senderAccount.setBalance(senderAccount.getBalance().subtract(payment.getAmount()));
        }
    }

    private Payment finalizePayment(BankAccount senderAccount, BankAccount recipientAccount, Payment payment) {
        payment.setStatus(FinancialStatus.RECEIVED);
        accountRepository.save(senderAccount);
        accountRepository.save(recipientAccount);
        return paymentRepository.save(payment);
    }

    private Payment finalizeCardPayment(BankAccount senderAccount, Payment payment, BigDecimal amount) {
        payment.setStatus(FinancialStatus.RECEIVED);
        payment.setAmount(amount);
        accountRepository.save(senderAccount);
        return paymentRepository.save(payment);
    }

    private BigDecimal generateRandomAmount() {
        Random randomAmount = new Random();
        return BigDecimal.valueOf(randomAmount.nextDouble(10000) + 1);
    }

    private void transferCurrency(BigDecimal amount, Currency senderCurrency,
                                  Currency receiverCurrency, Payment payment) {
        BigDecimal exchangeRate = currencyDataService.convertCurrency(
                senderCurrency.toString(), receiverCurrency.toString()
        ).getRate();
        BigDecimal convertedAmount = amount.multiply(exchangeRate);
        payment.setAmount(convertedAmount);
        payment.setCurrency(senderCurrency);
    }

    private BankIdentity getBankIdentity(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String identityId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!
        return identityRepository.findById(identityId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank not found.")
        );
    }
}
