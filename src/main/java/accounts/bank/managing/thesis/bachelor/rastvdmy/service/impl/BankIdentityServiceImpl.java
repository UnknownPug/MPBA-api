package accounts.bank.managing.thesis.bachelor.rastvdmy.service.impl;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankIdentityRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.AccessToken;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankAccount;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankIdentity;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.UserStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.AccessTokenRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankIdentityRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankAccountService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankIdentityService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.JwtService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BankIdentityServiceImpl extends Generator implements BankIdentityService {
    private final BankIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final AccessTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final BankAccountService accountService;

    @Autowired
    public BankIdentityServiceImpl(BankIdentityRepository identityRepository,
                                   RestTemplate restTemplate,
                                   UserRepository userRepository,
                                   AccessTokenRepository tokenRepository,
                                   JwtService jwtService, BankAccountService accountService) {
        super(restTemplate);
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    public List<BankIdentity> getBanks(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String userId = jwtService.extractId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));
        return identityRepository.findAllByUserId(user.getId());
    }

    public BankIdentity getBank(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String bankId = jwtService.extractId(token);
        return identityRepository.findById(bankId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested bank not found."));
    }

    public BankIdentity addBank(HttpServletRequest request, BankIdentityRequest identityRequest) {
        final String token = jwtService.extractToken(request);
        final String userId = jwtService.extractId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "User is blocked, operation is not available.");
        }

        if (identityRepository.findByBankName(identityRequest.bankName()) != null) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Bank with the same name already added.");
        }

        BankIdentity bankIdentity = BankIdentity.builder()
                .bankName(identityRequest.bankName())
                .bankNumber(generateBankNumber())
                .user(user)
                .build();

        AccessToken accessToken = getBankToken(bankIdentity);

        bankIdentity.setAccessTokens(List.of(accessToken));
        tokenRepository.save(accessToken);

        List<BankAccount> accounts = accountService.generateAccounts(bankIdentity);
        bankIdentity.setBankAccounts(accounts);

        return identityRepository.save(bankIdentity);
    }

    private AccessToken getBankToken(BankIdentity bankIdentity) {
        String bankToken = jwtService.generateTokenForBankIdentity(bankIdentity);
        return AccessToken.builder()
                .token(bankToken)
                .bankIdentity(bankIdentity)
                .build();
    }

    public void deleteBank(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String bankId = jwtService.extractId(token);
        BankIdentity bankIdentity = identityRepository.findById(bankId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested bank not found."));

        if (bankIdentity.getUser().getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "User is blocked, operation is not available.");
        }

        if (!bankIdentity.getBankAccounts().isEmpty()) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Make sure to delete all bank accounts first.");
        }

        identityRepository.delete(bankIdentity);
    }
}
