package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.DepositRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.DepositService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        LOG.debug("Getting all deposits ...");
        return ResponseEntity.ok(depositService.getAllDeposits());
    }

    @GetMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<Deposit>> filterDeposits(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.debug("Filtering deposits ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.debug("Sorting deposits by amount in ascending order ...");
                Pageable pageableAsc = PageRequest.of(page, size, Sort.by("depositAmount").ascending());
                return ResponseEntity.ok(depositService.filterAndSortDeposits(pageableAsc));
            case "desc":
                LOG.debug("Sorting deposits by amount in descending order ...");
                Pageable pageableDesc = PageRequest.of(page, size, Sort.by("depositAmount").descending());
                return ResponseEntity.ok(depositService.filterAndSortDeposits(pageableDesc));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Deposit> getAllDepositById(@PathVariable(value = "id") Long depositId) {
        LOG.debug("Getting deposit id: {} ...", depositId);
        return ResponseEntity.ok(depositService.getAllDepositById(depositId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Deposit> openDeposit(@RequestBody DepositRequest request) {
        LOG.debug("Creating deposit ...");
        return ResponseEntity.ok(depositService.openDeposit(request.cardNumber(), request.depositAmount(), request.description(), request.currency()));
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateDeposit(
            @PathVariable(value = "id") Long depositId,
            @RequestBody DepositRequest request) {
        LOG.debug("Updating deposit ...");
        depositService.updateDeposit(depositId, request.cardNumber(), request.description(), request.depositAmount(), request.currency());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeposit(@PathVariable(value = "id") Long depositId) {
        LOG.debug("Deleting deposit ...");
        depositService.deleteDeposit(depositId);
    }
}
