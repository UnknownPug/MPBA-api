package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.CardRequest;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

/**
 * Card service interface
 */
public interface CardService {

    /**
     * Get all cards
     * @return List of cards
     */
    List<Card> getAccountCards(UUID id, HttpServletRequest request);

    /**
     * Get card by card number
     *
     * @param cardId Card number
     * @return Card
     */
    Card getAccountCardById(UUID accountId, UUID cardId, HttpServletRequest request);

    /**
     * Add a card
     * @param cardRequest Card request
     * @return Card
     */
    Card addAccountCard(UUID id, HttpServletRequest request, CardRequest cardRequest) throws Exception;

    void connectCards(BankAccount account) throws Exception;

    /**
     * Update card status
     *
     * @param cardId Card number
     */
    void updateAccountCardStatus(UUID accountId, UUID cardId, HttpServletRequest request);

    /**
     * Remove card
     *
     * @param cardId Card number
     */
    void removeAccountCard(UUID accountId, UUID cardId, HttpServletRequest request);

    void removeAllCards(BankAccount account);
}
