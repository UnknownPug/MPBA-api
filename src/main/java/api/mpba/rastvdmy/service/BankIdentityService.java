package api.mpba.rastvdmy.service;

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
     * @param request the HTTP request containing user information
     * @return the added {@link BankIdentity} object
     * @throws Exception if an error occurs during the addition of the bank identity
     */
    BankIdentity addBank(HttpServletRequest request) throws Exception;

    /**
     * Deletes a bank identity associated with the user by its name.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank identity to be deleted
     */
    void deleteBank(HttpServletRequest request, String bankName);
}
