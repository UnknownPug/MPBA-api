package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
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
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public BankAccountServiceImpl(BankAccountRepository accountRepository,
                                  BankIdentityRepository bankIdentityRepository,
                                  JwtService jwtService,
                                  CardService cardService,
                                  UserProfileRepository userProfileRepository) {
        this.accountRepository = accountRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.jwtService = jwtService;
        this.cardService = cardService;
        this.userProfileRepository = userProfileRepository;
    }

    public List<BankAccount> getUserAccounts(HttpServletRequest request, String bankName) {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);
        List<BankAccount> accounts = accountRepository.findAllByBankIdentityId(bankIdentity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No accounts found.")
        );
        return accounts.stream().filter(account -> decryptAccountData(account, false)).collect(Collectors.toList());
    }

    public BankAccount getAccountById(HttpServletRequest request, String bankName, UUID accountId, String type) {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);

        List<BankAccount> accounts = accountRepository.findAllByBankIdentityId(bankIdentity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested account not found.")
        );

        boolean unmask = type.equals("visible");
        return accounts.stream()
                .filter(acc -> acc.getId().equals(accountId))
                .filter(acc -> decryptAccountData(acc, unmask))
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested account not found.")
                );
    }

    private boolean decryptAccountData(BankAccount account, boolean unmask) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            String decryptedAccountNumber = EncryptionUtil.decrypt(account.getAccountNumber(), secretKey);
            String decryptedIban = EncryptionUtil.decrypt(account.getIban(), secretKey);

            if (unmask) {
                account.setAccountNumber(decryptedAccountNumber);
                account.setIban(decryptedIban);
            } else {
                account.setAccountNumber(maskAccountData(decryptedAccountNumber));
                account.setIban(maskAccountData(decryptedIban));
            }
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    private String maskAccountData(String data) {
        int length = data.length();
        if (length <= 4) {
            return data;
        }
        return "*".repeat(length - 4) + data.substring(length - 4);
    }

    public Map<String, BigDecimal> getTotalBalance(HttpServletRequest request) {
        UserProfile userProfile = getUserData(request, jwtService, userProfileRepository);
        List<BankIdentity> bankIdentities = bankIdentityRepository.findAllByUserProfileId(userProfile.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No bank identities found.")
        );

        List<UUID> bankIdentitiesIds = bankIdentities.stream().map(BankIdentity::getId).toList();

        List<BankAccount> allAccounts = accountRepository.findAllByBankIdentitiesId(bankIdentitiesIds); // Assume this method fetches all bank accounts

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
    public BankAccount addAccount(HttpServletRequest request, String bankName) throws Exception {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);
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

        cardService.connectCards(mainAccount);
        availableCurrencies.remove(Currency.CZK);

        int numberOfAccounts = generateBalance.nextInt(Math.min(availableCurrencies.size(),
                MAX_AVAILABLE_ACCOUNTS - MIN_AVAILABLE_ACCOUNTS)) + MIN_AVAILABLE_ACCOUNTS;

        for (int i = 0; i < numberOfAccounts; i++) {
            BankAccount account = generateAccountData(
                    generateBalance, availableCurrencies.remove(i % availableCurrencies.size()), bankIdentity);

            cardService.connectCards(account);
        }
    }

    @Transactional
    protected BankAccount generateAccountData(Random generateBalance,
                                              Currency accountCurrency, BankIdentity bankIdentity) throws Exception {
        SecretKey secretKey = EncryptionUtil.getSecretKey();

        String encodedAccountNumber = EncryptionUtil.encrypt(generateAccountNumber(), secretKey);
        String encodedIban = EncryptionUtil.encrypt(generateIban(), secretKey);

        BankAccount account = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(generateBalance.nextInt(MAX_BALANCE) + MIN_BALANCE))
                .accountNumber(encodedAccountNumber)
                .currency(accountCurrency)
                .iban(encodedIban)
                .bankIdentity(bankIdentity)
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public void removeAccount(HttpServletRequest request, String bankName, UUID accountId) {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);

        List<BankAccount> accounts = accountRepository.findAllByBankIdentityId(bankIdentity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested account not found.")
        );

        BankAccount account = accounts.stream()
                .filter(acc -> acc.getId().equals(accountId))
                .findFirst().orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested account not found."));

        if (!account.getCards().isEmpty()) {
            cardService.removeAllCards(account);
        }
        accountRepository.delete(account);
    }

    @Transactional
    public void removeAllAccounts(HttpServletRequest request, String bankName) {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);

        List<BankAccount> accounts = accountRepository.findAllByBankIdentityId(bankIdentity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank Identity not found.")
        );
        if (accounts.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND,
                    "No accounts are found for the specified bank identity.");
        } else {
            accounts.forEach(account -> {
                if (!account.getCards().isEmpty()) {
                    cardService.removeAllCards(account);
                }
            });
        }
        accountRepository.deleteAll(accounts);
    }

    private BankIdentity getBankIdentity(HttpServletRequest request, String bankName) {
        UserProfile userProfile = getUserData(request, jwtService, userProfileRepository);

        return bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), bankName).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank Identity not found.")
        );
    }

    public static UserProfile getUserData(HttpServletRequest request, JwtService jwtService, UserProfileRepository userProfileRepository) {
        final String token = jwtService.extractToken(request);
        final String userEmail = jwtService.extractSubject(token);

        UserProfile userProfile = userProfileRepository.findByEmail(userEmail).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        return userProfile;
    }
}
