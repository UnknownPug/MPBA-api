package api.mpba.rastvdmy.service;

import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.entity.BankIdentity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Service for managing bank identities.
 */
public interface BankIdentityService {

    /**
     * Retrieves a list of bank identities associated with the user.
     *
     * @param request the HTTP request containing user information
     * @return a list of {@link BankIdentity} objects available to the user
     */
    List<BankIdentity> getBanks(HttpServletRequest request);

    /**
     * Retrieves a bank identity by its name.
     *
     * @param request the HTTP request containing user information
     * @param name    the name of the bank to retrieve
     * @return the {@link BankIdentity} associated with the specified name
     */
    BankIdentity getBankByName(HttpServletRequest request, String name);

    /**
     * Adds a new bank identity for the user.
     *
     * @param request         the HTTP request containing user information
     * @param identityRequest the request object containing the details of the bank identity to add
     * @return the newly created {@link BankIdentity}
     * @throws Exception if an error occurs during the bank identity creation process
     */
    BankIdentity addBank(HttpServletRequest request, BankIdentityRequest identityRequest) throws Exception;

    /**
     * Deletes a bank identity associated with the user by its name.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank identity to be deleted
     */
    void deleteBank(HttpServletRequest request, String bankName);
}
