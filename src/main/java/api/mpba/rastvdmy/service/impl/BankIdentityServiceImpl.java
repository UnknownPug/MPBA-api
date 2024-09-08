package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.BankAccountService;
import api.mpba.rastvdmy.service.BankIdentityService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.component.FinancialDataGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankIdentityServiceImpl extends FinancialDataGenerator implements BankIdentityService {
    private final BankIdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BankAccountService accountService;

    @Autowired
    public BankIdentityServiceImpl(BankIdentityRepository identityRepository,
                                   UserRepository userRepository,
                                   JwtService jwtService,
                                   BankAccountService accountService) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.accountService = accountService;
    }

    public List<BankIdentity> getBanks(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String userId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found."));
        return identityRepository.findAllByUserId(user.getId());
    }

    public BankIdentity getBank(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String bankId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!
        return identityRepository.findById(bankId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Requested bank not found."));
    }

    public BankIdentity addBank(HttpServletRequest request, BankIdentityRequest identityRequest) {
        final String token = jwtService.extractToken(request);
        final String userId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!

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


        List<BankAccount> accounts = accountService.generateAccounts(bankIdentity);
        bankIdentity.setBankAccounts(accounts);

        return identityRepository.save(bankIdentity);
    }

    public void deleteBank(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        final String bankId = jwtService.extractSubject(token); //FIXME: Don't forget that this is users' email!
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
