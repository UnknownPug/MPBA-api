package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankIdentityRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankIdentity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service for managing bank identities.
 */
public interface BankIdentityService {

    List<BankIdentity> getBanks(HttpServletRequest request);

    BankIdentity getBank(HttpServletRequest request);


    BankIdentity addBank(HttpServletRequest request, BankIdentityRequest identityRequest);

    void deleteBank(HttpServletRequest request);
}
