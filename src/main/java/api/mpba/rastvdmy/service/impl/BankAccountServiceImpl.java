package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.service.BankAccountService;
import api.mpba.rastvdmy.service.CardService;
import api.mpba.rastvdmy.service.UserValidationService;
import api.mpba.rastvdmy.service.generator.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.*;

/**
 * Service implementation for managing bank accounts, including account retrieval,
 * creation, and deletion functionalities.
 * It also handles user-related operations
 * related to bank accounts, ensuring that user data is appropriately encrypted and masked.
 */
@Service
public class BankAccountServiceImpl extends FinancialDataGenerator implements BankAccountService {
    /**
     * The minimum balance for a bank account.
     */
    private static final int MIN_BALANCE = 1000;

    /**
     * The maximum balance for a bank account.
     */
    private static final int MAX_BALANCE = 10000;

    /**
     * The maximum number of available accounts.
     */
    private static final int MAX_AVAILABLE_ACCOUNTS = 4;

    /**
     * The minimum number of available accounts.
     */
    private static final int MIN_AVAILABLE_ACCOUNTS = 1;

    private final BankAccountRepository accountRepository;
    private final BankIdentityRepository bankIdentityRepository;
    private final CardService cardService;
    private final UserValidationService userValidationService;

    /**
     * Constructs a new instance of {@link BankAccountServiceImpl}.
     *
     * @param accountRepository      the repository for bank account operations
     * @param bankIdentityRepository the repository for bank identity operations
     * @param cardService            the service for handling card operations
     * @param userValidationService  the service for extracting user token and getting user data from the request
     */
    @Autowired
    public BankAccountServiceImpl(BankAccountRepository accountRepository,
                                  BankIdentityRepository bankIdentityRepository,
                                  CardService cardService, UserValidationService userValidationService) {
        this.accountRepository = accountRepository;
        this.bankIdentityRepository = bankIdentityRepository;
        this.cardService = cardService;
        this.userValidationService = userValidationService;
    }

    /**
     * Retrieves a list of bank accounts associated with the specified bank for the user
     * identified by the request.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank
     * @return a list of bank accounts
     * @throws ApplicationException if no accounts are found for the user, or if there is an error during decryption
     */
    public List<BankAccount> getUserAccounts(HttpServletRequest request, String bankName) {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);
        List<BankAccount> accounts = accountRepository.findAllByBankIdentityId(bankIdentity.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "No accounts found.")
        );
        return accounts.stream()
                .filter(account -> decryptAccountData(account, false))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific bank account by its ID for the user identified by the request
     * and bank name.
     *
     * @param request   the HTTP request containing user information
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account
     * @param type      the type of visibility ("visible" to unmask)
     * @return the requested bank account
     * @throws ApplicationException if the requested account is not found
     */
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

    /**
     * Decrypts the account number and IBAN for a given bank account, optionally masking them.
     *
     * @param account the bank account to decrypt
     * @param unmask  whether to unmask the account number and IBAN
     * @return true if decryption was successful
     * @throws ApplicationException if there is an error during decryption
     */
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
                account.setIban(maskIban(decryptedIban));
            }
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    /**
     * Masks the account number, displaying only the last four digits.
     *
     * @param data the account number to mask
     * @return the masked account number
     */
    private String maskAccountData(String data) {
        int length = data.length();
        if (length <= 4) {
            return data;
        }
        return "*".repeat(length - 4) + data.substring(length - 4);
    }

    /**
     * Masks the IBAN, displaying only the first four and last four digits.
     *
     * @param data the IBAN to mask
     * @return the masked IBAN
     */
    private String maskIban(String data) {
        int length = data.length();
        if (length <= 8) {
            return data;
        }
        return data.substring(0, 4) + "****" + data.substring(length - 4);
    }

    /**
     * Retrieves the total balance across all bank accounts associated with the user.
     *
     * @param request the HTTP request containing user information
     * @return a map of total balances for each currency
     * @throws ApplicationException if no bank identities are found for the user
     */
    public Map<String, BigDecimal> getTotalBalance(HttpServletRequest request) {
        UserProfile userProfile = userValidationService.getUserData(request);
        List<BankIdentity> bankIdentities =
                bankIdentityRepository.findAllByUserProfileId(userProfile.getId()).orElseThrow(
                        () -> new ApplicationException(HttpStatus.NOT_FOUND, "No bank identities found.")
                );

        List<UUID> bankIdentitiesIds = bankIdentities.stream().map(BankIdentity::getId).toList();

        // Assume this method fetches all bank accounts
        List<BankAccount> allAccounts = accountRepository.findAllByBankIdentitiesId(bankIdentitiesIds);

        Map<String, BigDecimal> totalBalances = allAccounts.stream()
                .collect(Collectors.groupingBy(
                        account -> account.getCurrency().toString(),
                        Collectors.reducing(BigDecimal.ZERO, BankAccount::getBalance, BigDecimal::add)
                ));

        Stream.of("CZK", "USD", "EUR", "PLN", "UAH") // Ensure all required currencies are present in the map
                .forEach(currency -> totalBalances.putIfAbsent(currency, BigDecimal.ZERO));

        return totalBalances;
    }

    /**
     * Adds a new bank account for the user.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank
     * @return the created bank account
     * @throws Exception if an error occurs during account creation
     */
    @Transactional
    public BankAccount addAccount(HttpServletRequest request, String bankName) throws Exception {
        BankIdentity bankIdentity = getBankIdentity(request, bankName);
        Random generateBalance = new Random();

        BankAccount account = generateAccountData(generateBalance, Currency.getRandomCurrency(), bankIdentity);
        cardService.connectCards(account);

        return account;
    }

    /**
     * Connects multiple bank accounts to a user's bank identity.
     *
     * @param bankIdentity the bank identity to connect accounts to
     * @throws Exception if an error occurs during account creation
     */
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

    /**
     * Generates and saves a new bank account with random data for the specified bank identity.
     *
     * @param generateBalance the random number generator for account balance
     * @param accountCurrency the currency for the account
     * @param bankIdentity    the bank identity associated with the account
     * @return the generated bank account
     * @throws Exception if an error occurs during account generation
     */
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

    /**
     * Removes a specific bank account for the user identified by the request and bank name.
     *
     * @param request   the HTTP request containing user information
     * @param bankName  the name of the bank
     * @param accountId the ID of the bank account to be removed
     * @throws ApplicationException if the requested account is not found, or if there is an error during card removal
     */
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

    /**
     * Removes all bank accounts associated with the specified bank for the user identified by the request.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank
     * @throws ApplicationException if no bank identities or accounts are found for the user,
     *                              or if there is an error during card removal
     */
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

    /**
     * Retrieves the bank identity for the user identified by the request and bank name.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank
     * @return the bank identity
     * @throws ApplicationException if the bank identity is not found
     */
    private BankIdentity getBankIdentity(HttpServletRequest request, String bankName) {
        UserProfile userProfile = userValidationService.getUserData(request);

        return bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), bankName.trim()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Bank Identity not found.")
        );
    }
}
