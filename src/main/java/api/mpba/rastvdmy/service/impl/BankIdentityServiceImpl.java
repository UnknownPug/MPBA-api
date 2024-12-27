package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.BankAccountService;
import api.mpba.rastvdmy.service.BankIdentityService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.generator.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing bank identities, including retrieval,
 * addition, and deletion of bank identities connected to a user's profile.
 */
@Service
public class BankIdentityServiceImpl extends FinancialDataGenerator implements BankIdentityService {
    private final BankIdentityRepository identityRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final BankAccountService accountService;

    /**
     * Constructs a new instance of {@link BankIdentityServiceImpl}.
     *
     * @param identityRepository    the repository for bank identity operations
     * @param userProfileRepository the repository for user profile operations
     * @param jwtService            the service for handling JWT operations
     * @param accountService        the service for managing bank accounts
     */
    @Autowired
    public BankIdentityServiceImpl(BankIdentityRepository identityRepository,
                                   UserProfileRepository userProfileRepository,
                                   JwtService jwtService,
                                   BankAccountService accountService) {
        this.identityRepository = identityRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    /**
     * Retrieves a list of bank identities associated with the user identified by the request.
     *
     * @param request the HTTP request containing user information
     * @return a list of bank identities
     * @throws ApplicationException if the user is blocked or if no bank identities are found
     */
    @Cacheable(value = "bankIdentity")
    public List<BankIdentity> getBanks(HttpServletRequest request) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        return identityRepository.findAllByUserProfileId(userProfile.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User doesn't have any connected bank accounts.")
        );
    }

    /**
     * Retrieves a specific bank identity by name for the user identified by the request.
     *
     * @param request the HTTP request containing user information
     * @param name    the name of the bank
     * @return the bank identity
     * @throws ApplicationException if the user is blocked or if the bank identity is not found
     */
    @Cacheable(value = "bankIdentity", key = "#request.userPrincipal.name + '-' + #name")
    public BankIdentity getBankByName(HttpServletRequest request, String name) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        return identityRepository.findByNameAndConnectedToUserId(name.trim(), userProfile.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User doesn't have this specific bank."));
    }

    /**
     * Adds a new bank identity for the user identified by the request.
     *
     * @param request the HTTP request containing user information
     * @return the created bank identity
     * @throws Exception            if an error occurs during bank identity creation
     * @throws ApplicationException if the user is blocked or if a bank with the same name already exists
     */
    @Transactional
    public BankIdentity addBank(HttpServletRequest request) throws Exception {
        String bankName = SupportedBanks.getRandomBank();
        UserProfile userProfile = validateUserData(request, bankName);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        BankIdentity bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName(bankName)
                .bankNumber(generateBankNumber())
                .swift(generateSwift())
                .userProfile(userProfile)
                .build();

        bankIdentity = identityRepository.save(bankIdentity);
        accountService.connectAccounts(bankIdentity); // Calling Bank Account Service ...

        return bankIdentity;
    }

    /**
     * Deletes a bank identity associated with the user.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank to delete
     * @throws ApplicationException if the user is blocked, if the bank identity is not found, or if there are connected bank accounts
     */
    @CacheEvict(value = "bankIdentity", key = "#request.userPrincipal.name + '-' + #bankName")
    public void deleteBank(HttpServletRequest request, String bankName) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        BankIdentity bankIdentity =
                identityRepository.findByUserIdAndBankNameWithAccounts(userProfile.getId(), bankName.trim())
                        .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,
                                "Bank is not connected to the user."
                        ));

        if (!bankIdentity.getBankAccounts().isEmpty()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Make sure to delete all bank accounts first.");
        }

        identityRepository.delete(bankIdentity);
    }

    /**
     * Validates the user data and checks if the bank name already exists for the user.
     *
     * @param request  the HTTP request containing user information
     * @param bankName the name of the bank to validate
     * @return the user profile associated with the request
     * @throws ApplicationException if the user is blocked or if a bank with the same name already exists
     */
    private UserProfile validateUserData(HttpServletRequest request, String bankName) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (identityRepository
                .findByUserProfileIdAndBankName(userProfile.getId(), bankName)
                .isPresent()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Bank with the same name already added.");
        }

        return userProfile;
    }

    /**
     * Retrieves the user associated with the request.
     *
     * @param request the HTTP request containing user information
     * @return the user profile associated with the request
     * @throws ApplicationException if the user is not authorized
     */
    private UserProfile getUser(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String userEmail = jwtService.extractSubject(token);
        return userProfileRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApplicationException(HttpStatus.UNAUTHORIZED, "User not authorized."));
    }
}