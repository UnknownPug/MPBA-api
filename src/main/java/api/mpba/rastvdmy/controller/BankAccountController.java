package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.BankAccountMapper;
import api.mpba.rastvdmy.dto.request.BankAccountRequest;
import api.mpba.rastvdmy.dto.response.BankAccountResponse;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.service.BankAccountService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for managing bank accounts.
 * <p>
 * This controller provides endpoints for users to interact with their bank accounts,
 * including retrieving account information, adding new accounts, and removing accounts.
 * It utilizes the {@link BankAccountService} for business logic and {@link BankAccountMapper}
 * for mapping between request and response objects.
 * </p>
 */
@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/accounts")
public class BankAccountController {
    private final BankAccountMapper accountMapper;
    private final BankAccountService accountService;

    /**
     * Constructor for BankAccountController.
     *
     * @param accountMapper  The {@link BankAccountMapper} to be used.
     * @param accountService The {@link BankAccountService} to be used.
     */
    public BankAccountController(BankAccountMapper accountMapper, BankAccountService accountService) {
        this.accountMapper = accountMapper;
        this.accountService = accountService;
    }

    /**
     * Retrieves all bank accounts for the current user associated with a specific bank.
     *
     * @param request  The HTTP servlet request containing user information.
     * @param bankName The name of the bank for which to retrieve accounts.
     * @return A {@link ResponseEntity} containing a list of {@link BankAccountResponse}.
     */
    @GetMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<List<BankAccountResponse>> getUserAccounts(HttpServletRequest request,
                                                                     @PathVariable("name") String bankName) {
        logInfo("Getting accounts for current user ...");
        List<BankAccount> accounts = accountService.getUserAccounts(request, bankName);
        List<BankAccountResponse> response = accounts.stream()
                .map(account -> accountMapper.toResponse(convertToAccountRequest(account)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific bank account by its ID.
     *
     * @param request   The HTTP servlet request containing user information.
     * @param bankName  The name of the bank where the account is held.
     * @param accountId The ID of the account to retrieve.
     * @param type      The type of account (optional, defaults to "non-visible").
     * @return A {@link ResponseEntity} containing the requested {@link BankAccountResponse}.
     */
    @GetMapping(path = "/{name}/{accountId}", produces = "application/json")
    public ResponseEntity<BankAccountResponse> getAccountById(HttpServletRequest request,
                                                              @PathVariable("name") String bankName,
                                                              @PathVariable("accountId") UUID accountId,
                                                              @RequestParam(value = "type",
                                                                      defaultValue = "non-visible") String type) {
        logInfo("Getting account info ...");
        BankAccount account = accountService.getAccountById(request, bankName, accountId, type);
        BankAccountResponse response = accountMapper.toResponse(convertToAccountRequest(account));
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the total balances for all bank accounts associated with the current user.
     *
     * @param request The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} containing a map of total balances for all accounts.
     */
    @GetMapping(path = "/total", produces = "application/json")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance(HttpServletRequest request) {
        logInfo("Getting total balances for all bank accounts ...");
        Map<String, BigDecimal> totalBalances = accountService.getTotalBalance(request);
        return ResponseEntity.ok(totalBalances);
    }

    /**
     * Adds a new bank account for the current user associated with a specific bank.
     *
     * @param request  The HTTP servlet request containing user information.
     * @param bankName The name of the bank to connect the new account to.
     * @return A {@link ResponseEntity} containing the created {@link BankAccountResponse}.
     * @throws Exception If there is an error while adding the account.
     */
    @PostMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<BankAccountResponse> addAccount(HttpServletRequest request,
                                                          @PathVariable("name") String bankName) throws Exception {
        logInfo("Connecting to the bank to connect a bank account ...");
        BankAccount account = accountService.addAccount(request, bankName);
        BankAccountResponse response = accountMapper.toResponse(convertToAccountRequest(account));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Converts a {@link BankAccount} to a {@link BankAccountRequest}.
     *
     * @param account The {@link BankAccount} to convert.
     * @return The converted {@link BankAccountRequest}.
     */
    private static BankAccountRequest convertToAccountRequest(BankAccount account) {
        return new BankAccountRequest(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getIban()
        );
    }

    /**
     * Removes a specific bank account associated with the current user.
     *
     * @param request   The HTTP servlet request containing user information.
     * @param bankName  The name of the bank where the account is held.
     * @param accountId The ID of the account to remove.
     * @return A {@link ResponseEntity} with no content.
     */
    @DeleteMapping(path = "/{name}/{accountId}")
    public ResponseEntity<Void> removeAccount(HttpServletRequest request,
                                              @PathVariable("name") String bankName,
                                              @PathVariable("accountId") UUID accountId) {
        logInfo("Removing account for bank identity ...");
        accountService.removeAccount(request, bankName, accountId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes all bank accounts associated with the current user for a specific bank.
     *
     * @param request  The HTTP servlet request containing user information.
     * @param bankName The name of the bank for which to remove all accounts.
     * @return A {@link ResponseEntity} with no content.
     */
    @DeleteMapping(path = "/{name}")
    public ResponseEntity<Void> removeAllAccounts(HttpServletRequest request,
                                                  @PathVariable("name") String bankName) {
        logInfo("Removing all accounts ...");
        accountService.removeAllAccounts(request, bankName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        log.info(message, args);
    }
}