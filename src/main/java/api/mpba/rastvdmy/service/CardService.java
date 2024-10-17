package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

/**
 * Card service interface for managing card-related operations.
 */
public interface CardService {

    /**
     * Retrieves a list of cards associated with a specific bank account.
     *
     * @param bankName  the name of the bank associated with the account
     * @param accountId the unique identifier of the bank account
     * @param request   the HTTP request containing user information
     * @return a list of {@link Card} objects associated with the specified account
     */
    List<Card> getAccountCards(String bankName, UUID accountId, HttpServletRequest request);

    /**
     * Retrieves a specific card associated with a bank account by its unique identifier.
     *
     * @param bankName  the name of the bank associated with the account
     * @param accountId the unique identifier of the bank account
     * @param cardId    the unique identifier of the card to retrieve
     * @param request   the HTTP request containing user information
     * @param type      the type of card (e.g., debit, credit)
     * @return the {@link Card} associated with the specified account and card ID
     */
    Card getAccountCardById(String bankName, UUID accountId, UUID cardId, HttpServletRequest request, String type);

    /**
     * Adds a new card to the specified bank account.
     *
     * @param bankName  the name of the bank associated with the account
     * @param accountId the unique identifier of the bank account
     * @param request   the HTTP request containing user information
     * @return the newly created {@link Card}
     * @throws Exception if an error occurs during the card creation process
     */
    Card addAccountCard(String bankName, UUID accountId, HttpServletRequest request) throws Exception;

    /**
     * Connects the specified bank account with a card.
     *
     * @param account the {@link BankAccount} to connect with the card
     * @throws Exception if an error occurs during the card connection process
     */
    void connectCards(BankAccount account) throws Exception;

    /**
     * Removes a specific card from the specified bank account.
     *
     * @param bankName  the name of the bank associated with the account
     * @param accountId the unique identifier of the bank account
     * @param cardId    the unique identifier of the card to be removed
     * @param request   the HTTP request containing user information
     */
    void removeAccountCard(String bankName, UUID accountId, UUID cardId, HttpServletRequest request);

    /**
     * Removes all cards associated with the specified bank account.
     *
     * @param account the {@link BankAccount} from which all cards should be removed
     */
    void removeAllCards(BankAccount account);
}
