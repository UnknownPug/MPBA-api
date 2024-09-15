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

    List<BankAccount> getUserAccounts(HttpServletRequest request);

    BankAccount getAccountById(UUID accountId, HttpServletRequest request);

    Map<String, BigDecimal> getTotalBalance();

    BankAccount addAccount(HttpServletRequest request) throws Exception;

    void connectAccounts(BankIdentity bankIdentity) throws Exception;

    void removeAccount(UUID accountId, HttpServletRequest request);

    void removeAllAccounts(HttpServletRequest request);
}
