package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This interface represents the service for managing bank accounts.
 */
public interface BankAccountService {

    /**
     * Retrieves a list of bank accounts associated with the specified bank for the user.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank to filter accounts
     * @return a list of {@link BankAccount} objects associated with the user and specified bank
     */
    List<BankAccount> getUserAccounts(HttpServletRequest request, String bankName);

    /**
     * Retrieves a specific bank account by its ID.
     *
     * @param request   the HTTP request containing user information
     * @param bankName  the name of the bank to which the account belongs
     * @param accountId the UUID of the account to retrieve
     * @param type      the type of the bank account
     * @return the {@link BankAccount} associated with the given ID
     */
    BankAccount getAccountById(HttpServletRequest request, String bankName, UUID accountId, String type);

    /**
     * Calculates the total balance for all bank accounts associated with the user.
     *
     * @param request the HTTP request containing user information
     * @return a map where the keys are bank names and the values are the total balances for each bank
     */
    Map<String, BigDecimal> getTotalBalance(HttpServletRequest request);

    /**
     * Adds a new bank account for the user in the specified bank.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank where the account will be created
     * @return the newly created {@link BankAccount}
     * @throws Exception if an error occurs during the account creation process
     */
    BankAccount addAccount(HttpServletRequest request, String bankName) throws Exception;

    /**
     * Connects a bank account to a user's bank identity.
     *
     * @param bankIdentity the bank identity object containing information to connect accounts
     * @throws Exception if an error occurs during the account connection process
     */
    void connectAccounts(BankIdentity bankIdentity) throws Exception;

    /**
     * Removes a specific bank account for the user.
     *
     * @param request   the HTTP request containing user information
     * @param bankName  the name of the bank from which the account will be removed
     * @param accountId the UUID of the account to be removed
     */
    void removeAccount(HttpServletRequest request, String bankName, UUID accountId);

    /**
     * Removes all bank accounts associated with the specified bank for the user.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank whose accounts will be removed
     */
    void removeAllAccounts(HttpServletRequest request, String bankName);
}
