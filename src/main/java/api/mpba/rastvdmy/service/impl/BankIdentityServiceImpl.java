package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.BankAccountService;
import api.mpba.rastvdmy.service.BankIdentityService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.utils.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BankIdentityServiceImpl extends FinancialDataGenerator implements BankIdentityService {
    private final BankIdentityRepository identityRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final BankAccountService accountService;

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

    @Cacheable(value = "bankIdentity", key = "#request.userPrincipal.name + '-' + #name")
    public BankIdentity getBankByName(HttpServletRequest request, String name) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        return identityRepository.findByNameAndConnectedToUserId(name, userProfile.getId()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User doesn't have this specific bank."));
    }

    @Transactional
    public BankIdentity addBank(HttpServletRequest request, BankIdentityRequest identityRequest) throws Exception {
        UserProfile userProfile = validateUserData(request, identityRequest);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (identityRequest.bankName().isEmpty() || identityRequest.bankName().isBlank()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Bank name must not be empty.");
        }

        BankIdentity bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName(identityRequest.bankName())
                .bankNumber(generateBankNumber())
                .swift(generateSwift())
                .userProfile(userProfile)
                .build();

        bankIdentity = identityRepository.save(bankIdentity);
        accountService.connectAccounts(bankIdentity); // Calling Bank Account Service ...

        return bankIdentity;
    }

    @CacheEvict(value = "bankIdentity", key = "#request.userPrincipal.name + '-' + #bankName")
    public void deleteBank(HttpServletRequest request, String bankName) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        BankIdentity bankIdentity = identityRepository.findByUserIdAndBankNameWithAccounts(userProfile.getId(), bankName)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND,
                        "User is not found or bank with the given name is not connected to the user."
                ));

        if (!bankIdentity.getBankAccounts().isEmpty()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Make sure to delete all bank accounts first.");
        }

        identityRepository.delete(bankIdentity);
    }

    private UserProfile validateUserData(HttpServletRequest request, BankIdentityRequest identityRequest) {
        UserProfile userProfile = getUser(request);

        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (identityRepository.findByUserProfileIdAndBankName(userProfile.getId(), identityRequest.bankName()).isPresent()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Bank with the same name already added.");
        }

        return userProfile;
    }

    private UserProfile getUser(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String userEmail = jwtService.extractSubject(token);
        return userProfileRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));
    }
}