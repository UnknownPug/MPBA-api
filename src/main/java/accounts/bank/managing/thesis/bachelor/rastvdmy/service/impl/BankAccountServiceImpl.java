package accounts.bank.managing.thesis.bachelor.rastvdmy.service.impl;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankAccount;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankIdentity;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankAccountRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankIdentityRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankAccountService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.CardService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.JwtService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class BankAccountServiceImpl extends Generator implements BankAccountService {
    private static final int MIN_BALANCE = 100;
    private static final int MAX_BALANCE = 10000;
    private static final int MAX_AVAILABLE_ACCOUNTS = 4;
    private static final int MIN_AVAILABLE_ACCOUNTS = 1;
    private final BankAccountRepository accountRepository;
    private final BankIdentityRepository bankIdentityRepository;
    private final JwtService jwtService;
    private final CardService cardService;

    @Autowired
    public BankAccountServiceImpl(BankAccountRepository accountRepository,
                                  RestTemplate restTemplate,
                                  BankIdentityRepository bankIdentityRepository,
                                  JwtService jwtService,
                                  CardService cardService) {
        super(restTemplate);
        this.accountRepository = accountRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.jwtService = jwtService;
        this.cardService = cardService;
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

    public BankAccount addAccount(HttpServletRequest request) {
        BankIdentity bankIdentity = getBankIdentity(request);

        Random generateBalance = new Random();

        BankAccount account = BankAccount.builder()
                .balance(BigDecimal.valueOf(generateBalance.nextInt(MAX_BALANCE) + MIN_BALANCE))
                .accountNumber(generateAccountNumber())
                .currency(Currency.getRandomCurrency())
                .swift(generateSwift())
                .iban(generateIban())
                .bankIdentity(bankIdentity)
                .build();
        account.setCards(cardService.generateCards(account.getId()));
        return accountRepository.save(account);
    }

    public List<BankAccount> generateAccounts(BankIdentity identity) {
        List<Currency> availableCurrencies = new ArrayList<>(Arrays.asList(Currency.values()));
        Collections.shuffle(availableCurrencies);

        List<BankAccount> accounts = new ArrayList<>();
        Random generateBalance = new Random();

        // Ensure at least ibe account with CZK currency
        BankAccount account = BankAccount.builder()
                .balance(BigDecimal.valueOf(generateBalance.nextInt(MAX_BALANCE) + MIN_BALANCE))
                .accountNumber(generateAccountNumber())
                .currency(Currency.CZK)
                .swift(generateSwift())
                .iban(generateIban())
                .bankIdentity(identity)
                .build();

        account.setCards(cardService.generateCards(account.getId()));
        accounts.add(account);
        availableCurrencies.remove(Currency.CZK);

        int numberOfAccounts = generateBalance.nextInt(
                Math.min(availableCurrencies.size(), MAX_AVAILABLE_ACCOUNTS) + MIN_AVAILABLE_ACCOUNTS
        );

        accounts.addAll(
                IntStream.range(0, numberOfAccounts)
                        .mapToObj(i -> BankAccount.builder()
                                .balance(BigDecimal.valueOf(generateBalance.nextInt(MAX_BALANCE) + MIN_BALANCE))
                                .accountNumber(generateAccountNumber())
                                .currency(availableCurrencies.remove(i))
                                .swift(generateSwift())
                                .iban(generateIban())
                                .bankIdentity(identity)
                                .cards(cardService.generateCards(account.getId()))
                                .build()
                        ).toList()
        );
        return accountRepository.saveAll(accounts);
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
        final String bankIdentityId = jwtService.extractId(token);
        return bankIdentityRepository.findById(bankIdentityId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank identity not found.")
        );
    }
}
