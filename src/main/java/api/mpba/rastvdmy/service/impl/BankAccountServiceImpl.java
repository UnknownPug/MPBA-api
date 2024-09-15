package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.BankAccountService;
import api.mpba.rastvdmy.service.CardService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.utils.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.*;

@Service
public class BankAccountServiceImpl extends FinancialDataGenerator implements BankAccountService {
    private static final int MIN_BALANCE = 100;
    private static final int MAX_BALANCE = 10000;
    private static final int MAX_AVAILABLE_ACCOUNTS = 4;
    private static final int MIN_AVAILABLE_ACCOUNTS = 1;
    private final BankAccountRepository accountRepository;
    private final BankIdentityRepository bankIdentityRepository;
    private final JwtService jwtService;
    private final CardService cardService;
    private final UserRepository userRepository;

    @Autowired
    public BankAccountServiceImpl(BankAccountRepository accountRepository,
                                  BankIdentityRepository bankIdentityRepository,
                                  JwtService jwtService,
                                  CardService cardService, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.jwtService = jwtService;
        this.cardService = cardService;
        this.userRepository = userRepository;
    }

    public List<BankAccount> getUserAccounts(HttpServletRequest request) {
        BankIdentity bankIdentity = getBankIdentity(request);
        return accountRepository.findAllByBankIdentityId(bankIdentity.getId());
    }

    public BankAccount getAccountById(UUID id, HttpServletRequest request) {
        BankIdentity bankIdentity = getBankIdentity(request);

        BankAccount account = accountRepository.findByBankIdentityIdAndId(bankIdentity.getId(), id);
        if (account == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.");
        }
        return account;
    }

    public Map<String, BigDecimal> getTotalBalance() {
        List<BankAccount> allAccounts = accountRepository.findAll(); // Assume this method fetches all bank accounts

        Map<String, BigDecimal> totalBalances = allAccounts.stream()
                .collect(Collectors.groupingBy(
                        account -> account.getCurrency().toString(),
                        Collectors.reducing(BigDecimal.ZERO, BankAccount::getBalance, BigDecimal::add)
                ));

        Stream.of("CZK", "USD", "EUR", "PLN", "UAH") // Ensure all required currencies are present in the map
                .forEach(currency -> totalBalances.putIfAbsent(currency, BigDecimal.ZERO));

        return totalBalances;
    }

    @Transactional
    public BankAccount addAccount(HttpServletRequest request) throws Exception {
        BankIdentity bankIdentity = getBankIdentity(request);
        Random generateBalance = new Random();

        BankAccount account = generateAccountData(generateBalance, Currency.getRandomCurrency(), bankIdentity);
        cardService.connectCards(account);
        return account;
    }

    @Transactional
    public void connectAccounts(BankIdentity bankIdentity) throws Exception {
        List<Currency> availableCurrencies = new ArrayList<>(Arrays.asList(Currency.values()));
        Collections.shuffle(availableCurrencies);


        Random generateBalance = new Random();
        BankAccount mainAccount = generateAccountData(generateBalance, Currency.CZK, bankIdentity);

        try {
            cardService.connectCards(mainAccount);
        } catch (Exception e) {
            throw new ApplicationException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error while connecting cards: " + e.getMessage()
            );
        }


        availableCurrencies.remove(Currency.CZK);

        int numberOfAccounts = generateBalance.nextInt(Math.min(availableCurrencies.size(),
                        MAX_AVAILABLE_ACCOUNTS - MIN_AVAILABLE_ACCOUNTS)) + MIN_AVAILABLE_ACCOUNTS;

        for (int i = 0; i < numberOfAccounts; i++) {
            BankAccount account = generateAccountData(
                    generateBalance,
                    availableCurrencies.remove(i % availableCurrencies.size()), bankIdentity);
            try {
                cardService.connectCards(account); // Connect cards to the saved account
            } catch (Exception e) {
                throw new ApplicationException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Error while connecting cards: " + e.getMessage()
                );
            }
        }
    }

    @Transactional
    protected BankAccount generateAccountData(Random generateBalance,
                                              Currency accountCurrency, BankIdentity bankIdentity) throws Exception {
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encodedAccountNumber = EncryptionUtil.encrypt(
                generateAccountNumber(), secretKey, EncryptionUtil.generateIv()
        );

        String encodedIban = EncryptionUtil.encrypt(
                generateIban(), secretKey, EncryptionUtil.generateIv()
        );

        BankAccount account = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(generateBalance.nextInt(MAX_BALANCE) + MIN_BALANCE))
                .accountNumber(encodedAccountNumber)
                .currency(accountCurrency)
                .swift(generateSwift())
                .iban(encodedIban)
                .bankIdentity(bankIdentity)
                .build();
        return accountRepository.save(account);
    }

    public void removeAccount(UUID accountId, HttpServletRequest request) {
        BankIdentity bankIdentity = getBankIdentity(request);

        BankAccount account = accountRepository.findByBankIdentityIdAndId(bankIdentity.getId(), accountId);

        if (account == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Account not found.");
        }
        accountRepository.delete(account);
    }

    public void removeAllAccounts(HttpServletRequest request) {
        BankIdentity bankIdentity = getBankIdentity(request);

        List<BankAccount> accounts = accountRepository.findAllByBankIdentityId(bankIdentity.getId());

        if (accounts.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "No accounts found.");
        }
        accountRepository.deleteAll(accounts);
    }

    private BankIdentity getBankIdentity(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String userEmail = jwtService.extractSubject(token);

        User user = userRepository.findByEmail(userEmail).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );

        return bankIdentityRepository.findByUserId(user.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank identity not found.")
        );
    }
}
