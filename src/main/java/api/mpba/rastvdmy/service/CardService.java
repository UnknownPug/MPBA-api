package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

/**
 * Card service interface
 */
public interface CardService {

    List<Card> getAccountCards(String bankName, UUID accountId, HttpServletRequest request);

    Card getAccountCardById(String bankName, UUID accountId, UUID cardId, HttpServletRequest request, String type);

    Card addAccountCard(String bankName, UUID accountId, HttpServletRequest request) throws Exception;

    void connectCards(BankAccount account) throws Exception;

     void removeAccountCard(String bankName, UUID accountId, UUID cardId, HttpServletRequest request);

    void removeAllCards(BankAccount account);
}
