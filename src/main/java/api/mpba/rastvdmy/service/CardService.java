package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.Card;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Card service interface
 */
public interface CardService {

    List<Card> getAccountCards(String bankName, String accountNumber, HttpServletRequest request);

    Card getAccountCardByNumber(String bankName, String accountNumber, String cardNumber, HttpServletRequest request);

    Card addAccountCard(String bankName, String accountNumber, HttpServletRequest request) throws Exception;

    void connectCards(BankAccount account) throws Exception;

     void removeAccountCard(String bankName, String accountNumber, String cardNumber, HttpServletRequest request);

    void removeAllCards(BankAccount account);
}
