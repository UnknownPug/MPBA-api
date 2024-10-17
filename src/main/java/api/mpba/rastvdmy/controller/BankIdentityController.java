package api.mpba.rastvdmy.controller;

import api.mpba.rastvdmy.controller.mapper.BankIdentityMapper;
import api.mpba.rastvdmy.dto.request.BankIdentityRequest;
import api.mpba.rastvdmy.dto.response.BankIdentityResponse;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.service.BankIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing bank identities.
 * <p>
 * This controller provides endpoints for users to interact with bank identities,
 * including retrieving bank information, adding new banks, and deleting bank identities.
 * It utilizes the {@link BankIdentityService} for business logic and {@link BankIdentityMapper}
 * for mapping between request and response objects.
 * </p>
 */
@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/banks")
public class BankIdentityController {
    private final static Logger LOG = LoggerFactory.getLogger(BankIdentityController.class);
    private final BankIdentityService identityService;
    private final BankIdentityMapper identityMapper;

    /**
     * Constructor for BankIdentityController.
     *
     * @param identityService The {@link BankIdentityService} to be used.
     * @param identityMapper  The {@link BankIdentityMapper} to be used.
     */
    @Autowired
    public BankIdentityController(BankIdentityService identityService, BankIdentityMapper identityMapper) {
        this.identityService = identityService;
        this.identityMapper = identityMapper;
    }

    /**
     * Retrieves all bank identities.
     *
     * @param request The HTTP servlet request containing user information.
     * @return A {@link ResponseEntity} containing a list of {@link BankIdentityResponse}.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<BankIdentityResponse>> getBanks(HttpServletRequest request) {
        logInfo("Getting bank identities ...");
        List<BankIdentity> identities = identityService.getBanks(request);
        List<BankIdentityResponse> response = identities.stream()
                .map(identity -> identityMapper.toResponse(
                        new BankIdentityRequest(
                                identity.getBankName(),
                                identity.getBankNumber(),
                                identity.getSwift())
                )).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific bank identity by its name.
     *
     * @param request The HTTP servlet request containing user information.
     * @param name    The name of the bank to retrieve.
     * @return A {@link ResponseEntity} containing the requested {@link BankIdentityResponse}.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{name}", produces = "application/json")
    public ResponseEntity<BankIdentityResponse> getBankByName(HttpServletRequest request,
                                                              @PathVariable("name") String name) {
        logInfo("Getting bank info ...");
        BankIdentity identity = identityService.getBankByName(request, name);
        BankIdentityResponse response = identityMapper.toResponse(new BankIdentityRequest(
                identity.getBankName(),
                identity.getBankNumber(),
                identity.getSwift())
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a new bank identity.
     *
     * @param request         The HTTP servlet request containing user information.
     * @param identityRequest The request body containing bank identity details.
     * @return A {@link ResponseEntity} containing the created {@link BankIdentityResponse}.
     * @throws Exception If there is an error while adding the bank identity.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BankIdentityResponse> addBank(
            HttpServletRequest request,
            @Valid @RequestBody BankIdentityRequest identityRequest) throws Exception {
        logInfo("Connecting to the bank API to add bank ...");
        BankIdentity identity = identityService.addBank(request, identityRequest);
        BankIdentityResponse response = identityMapper.toResponse(new BankIdentityRequest(
                identity.getBankName(),
                identity.getBankNumber(),
                identity.getSwift())
        );
        return ResponseEntity.accepted().body(response);
    }

    /**
     * Deletes a bank identity by its name.
     *
     * @param request  The HTTP servlet request containing user information.
     * @param bankName The name of the bank to delete.
     * @return A {@link ResponseEntity} with no content.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{name}")
    public ResponseEntity<Void> deleteBank(HttpServletRequest request, @PathVariable("name") String bankName) {
        logInfo("Removing bank identity ...");
        identityService.deleteBank(request, bankName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Logs informational messages to the console.
     *
     * @param message The message to log.
     * @param args    Optional arguments to format the message.
     */
    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
