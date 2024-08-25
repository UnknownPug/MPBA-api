package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.controller.mapper.BankIdentityMapper;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankIdentityRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.BankIdentityResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankIdentity;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@PreAuthorize("hasRole('ROLE_DEFAULT')")
@RequestMapping(path = "/api/v1/banks")
public class BankIdentityController {
    private final static Logger LOG = LoggerFactory.getLogger(BankIdentityController.class);
    private final BankIdentityService identityService;
    private final BankIdentityMapper identityMapper;

    @Autowired
    public BankIdentityController(BankIdentityService identityService, BankIdentityMapper identityMapper) {
        this.identityService = identityService;
        this.identityMapper = identityMapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<BankIdentityResponse>> getBanks(HttpServletRequest request) {
        logInfo("Getting bank identities ...");
        List<BankIdentity> identities = identityService.getBanks(request);
        List<BankIdentityResponse> response = identities.stream()
                .map(identity -> identityMapper.toResponse(
                        new BankIdentityRequest(identity.getBankName()))).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/me", produces = "application/json")
    public ResponseEntity<BankIdentityResponse> getBank(HttpServletRequest request) {
        logInfo("Getting bank info ...");
        BankIdentity identity = identityService.getBank(request);
        BankIdentityResponse response = identityMapper.toResponse(new BankIdentityRequest(identity.getBankName()));
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<BankIdentityResponse> addBank(
            HttpServletRequest request,
            @Valid @RequestBody BankIdentityRequest identityRequest) {
        logInfo("Connecting to the bank API to add bank ...");
        BankIdentity identity = identityService.addBank(request, identityRequest);
        BankIdentityResponse response = identityMapper.toResponse(new BankIdentityRequest(identity.getBankName()));
        return ResponseEntity.accepted().body(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/me")
    public ResponseEntity<Void> removeMyBank(HttpServletRequest request) {
        logInfo("Removing bank identity ...");
        identityService.deleteBank(request);
        return ResponseEntity.noContent().build();
    }

    private void logInfo(String message, Object... args) {
        LOG.info(message, args);
    }
}
