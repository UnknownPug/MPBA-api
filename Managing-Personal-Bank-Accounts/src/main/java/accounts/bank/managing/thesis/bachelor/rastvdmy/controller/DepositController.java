package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.DepositRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.DepositService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/deposit")
public class DepositController {

    private static final Logger LOG = LoggerFactory.getLogger(DepositController.class);
    private final DepositService depositService;

    @Autowired
    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Deposit>> getAllDeposits() {
        LOG.info("Getting all deposits");
        return ResponseEntity.ok(depositService.getAllDeposits());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Deposit> getAllDepositById(@PathVariable(value = "id") Long depositId) {
        LOG.info("Getting all deposits");
        return ResponseEntity.ok(depositService.getAllDepositById(depositId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Deposit> createDeposit(@RequestBody DepositRequest request) {
        if (request.description() == null || request.referenceNumber() == null) {
            throw new IllegalArgumentException("Request is not valid.");
        }
        return ResponseEntity.ok(depositService.createDeposit(request.description(), request.referenceNumber()));
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateDeposit(@PathVariable(value = "id") Long depositId, @RequestBody DepositRequest request) {
        if (request.description() == null || request.referenceNumber() == null) {
            throw new IllegalArgumentException("Request is not valid.");
        }
        depositService.updateDeposit(depositId, request.description(), request.referenceNumber());
    }

    @PatchMapping(path = "/{id}/description")
    @ResponseStatus(HttpStatus.OK)
    public void updateDepositDescription(@PathVariable(value = "id") Long depositId, @RequestBody DepositRequest request) {
        if (request.description() == null) {
            throw new IllegalArgumentException("Request is not valid.");
        }
        depositService.updateDepositDescription(depositId, request.description());
    }

    @PatchMapping(path = "/{id}/reference")
    @ResponseStatus(HttpStatus.OK)
    public void updateDepositReferenceNumber(@PathVariable(value = "id") Long depositId, @RequestBody DepositRequest request) {
        if (request.referenceNumber() == null) {
            throw new IllegalArgumentException("Request is not valid.");
        }
        depositService.updateDepositReferenceNumber(depositId, request.referenceNumber());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeposit(@PathVariable(value = "id") Long depositId) {
        depositService.deleteDeposit(depositId);
    }
}
