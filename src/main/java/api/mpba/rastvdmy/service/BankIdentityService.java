package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.entity.BankIdentity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service for managing bank identities.
 */
public interface BankIdentityService {

    List<BankIdentity> getBanks(HttpServletRequest request);

    BankIdentity getBankByName(HttpServletRequest request, String name);

    BankIdentity addBank(HttpServletRequest request, BankIdentityRequest identityRequest) throws Exception;

    void deleteBank(HttpServletRequest request, String bankName);
}
