package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.BankAccountMapper;
import api.mpba.rastvdmy.dto.request.BankAccountRequest;
import api.mpba.rastvdmy.dto.response.BankAccountResponse;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.service.BankAccountService;
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

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<List<BankAccountResponse>> getUserAccounts(HttpServletRequest request,
                                                                     @PathVariable("name") String bankName) {
        logInfo("Getting accounts for current user ...");
        List<BankAccount> accounts = accountService.getUserAccounts(request, bankName);
        List<BankAccountResponse> response = accounts.stream()
                .map(account -> accountMapper.toResponse(
                        new BankAccountRequest(
                                account.getId(),
                                account.getAccountNumber(),
                                account.getBalance(),
                                account.getCurrency(),
                                account.getIban()
                        )
                )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{name}/{accountId}", produces = "application/json")
    public ResponseEntity<BankAccountResponse> getAccountById(HttpServletRequest request,
                                                              @PathVariable("name") String bankName,
                                                              @PathVariable("accountId") UUID accountId,
                                                              @RequestParam(value = "type",
                                                                      defaultValue = "non-visible") String type) {
        logInfo("Getting account info ...");
        BankAccount account = accountService.getAccountById(request, bankName, accountId, type);
        BankAccountResponse response = accountMapper.toResponse(
                new BankAccountRequest(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getCurrency(),
                        account.getIban()
                )
        );
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/total", produces = "application/json")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance(HttpServletRequest request) {
        logInfo("Getting total balances for all bank accounts ...");
        Map<String, BigDecimal> totalBalances = accountService.getTotalBalance(request);
        return ResponseEntity.ok(totalBalances);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<BankAccountResponse> addAccount(HttpServletRequest request,
                                                          @PathVariable("name") String bankName) throws Exception {
        logInfo("Connecting to the bank to connect a bank account ...");
        BankAccount account = accountService.addAccount(request, bankName);
        BankAccountResponse response = accountMapper.toResponse(
                new BankAccountRequest(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getCurrency(),
                        account.getIban()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{name}/{accountId}")
    public ResponseEntity<Void> removeAccount(HttpServletRequest request,
                                              @PathVariable("name") String bankName,
                                              @PathVariable("accountId") UUID accountId) {
        logInfo("Removing account for bank identity ...");
        accountService.removeAccount(request, bankName, accountId);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{name}")
    public ResponseEntity<Void> removeAllAccounts(HttpServletRequest request,
                                                  @PathVariable("name") String bankName) {
        logInfo("Removing all accounts ...");
        accountService.removeAllAccounts(request, bankName);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}