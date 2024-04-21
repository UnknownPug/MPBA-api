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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<List<Deposit>> getAllDeposits() {
        LOG.info("Getting all deposits ...");
        return ResponseEntity.ok(depositService.getAllDeposits());
    }

    @GetMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Page<Deposit>> filterDeposits(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.info("Filtering deposits ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.info("Sorting deposits by amount in ascending order ...");
                Pageable pageableAsc = PageRequest.of(page, size, Sort.by("depositAmount").ascending());
                return ResponseEntity.ok(depositService.filterAndSortDeposits(pageableAsc));
            case "desc":
                LOG.info("Sorting deposits by amount in descending order ...");
                Pageable pageableDesc = PageRequest.of(page, size, Sort.by("depositAmount").descending());
                return ResponseEntity.ok(depositService.filterAndSortDeposits(pageableDesc));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Deposit> getDepositById(@PathVariable(value = "id") Long depositId) {
        LOG.info("Getting deposit id: {} ...", depositId);
        return ResponseEntity.ok(depositService.getDepositById(depositId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Deposit> openDeposit(@RequestBody DepositRequest request) {
        LOG.info("Creating deposit ...");
        return ResponseEntity.ok(depositService.openDeposit(request.cardNumber(), request.depositAmount(), request.description(), request.currency()));
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void updateDeposit(
            @PathVariable(value = "id") Long depositId,
            @RequestBody DepositRequest request) {
        LOG.info("Updating deposit ...");
        depositService.updateDeposit(depositId, request.cardNumber(), request.description(), request.depositAmount(), request.currency());
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public void deleteDeposit(@PathVariable(value = "id") Long depositId) {
        LOG.info("Deleting deposit ...");
        depositService.deleteDeposit(depositId);
    }
}
