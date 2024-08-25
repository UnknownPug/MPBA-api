package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper.BankAccountMapper;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankAccountRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.BankAccountResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankAccount;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankAccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/accounts")
public class BankAccountController {
    private static final Logger LOG = LoggerFactory.getLogger(BankAccountController.class);
    private final BankAccountMapper accountMapper;
    private final BankAccountService accountService;

    @Autowired
    public BankAccountController(BankAccountMapper accountMapper, BankAccountService accountService) {
        this.accountMapper = accountMapper;
        this.accountService = accountService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<BankAccountResponse>> getUserAccounts(HttpServletRequest request) {
        logInfo("Getting accounts for current user ...");
        List<BankAccount> accounts = accountService.getUserAccounts(request);
        List<BankAccountResponse> response = accounts.stream()
                .map(account -> accountMapper.toResponse(
                        new BankAccountRequest(
                                account.getBalance(),
                                account.getAccountNumber(),
                                account.getSwift(),
                                account.getIban()
                        )
                )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<BankAccountResponse> getAccount(
            @PathVariable("id") UUID accountId, HttpServletRequest request) {
        logInfo("Getting account info ...");
        BankAccount account = accountService.getAccountById(accountId, request);
        BankAccountResponse response = accountMapper.toResponse(
                new BankAccountRequest(
                        account.getBalance(),
                        account.getAccountNumber(),
                        account.getSwift(),
                        account.getIban()
                )
        );
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/total", produces = "application/json")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance() {
        logInfo("Getting total balances for all bank accounts ...");
        Map<String, BigDecimal> totalBalances = accountService.getTotalBalance();
        return ResponseEntity.ok(totalBalances);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BankAccountResponse> addBankAccount(HttpServletRequest request) {
        logInfo("Connecting to the bank to connect a bank account ...");
        BankAccount account = accountService.addAccount(request);
        BankAccountResponse response = accountMapper.toResponse(
                new BankAccountRequest(
                        account.getBalance(),
                        account.getAccountNumber(),
                        account.getSwift(),
                        account.getIban()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> removeAccount(@PathVariable("id") UUID accountId, HttpServletRequest request) {
        logInfo("Removing account for bank identity ...");
        accountService.removeAccount(accountId, request);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public ResponseEntity<Void> removeAllAccounts(HttpServletRequest request) {
        logInfo("Removing all accounts ...");
        accountService.removeAllAccounts(request);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}