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

    List<BankAccount> getUserAccounts(HttpServletRequest request, String bankName);

    BankAccount getAccountById(HttpServletRequest request, String bankName, UUID accountId);

    Map<String, BigDecimal> getTotalBalance(HttpServletRequest request);

    BankAccount addAccount(HttpServletRequest request, String bankName) throws Exception;

    void connectAccounts(BankIdentity bankIdentity) throws Exception;

    void removeAccount(HttpServletRequest request, String bankName, UUID accountId);

    void removeAllAccounts(HttpServletRequest request, String bankName);
}
