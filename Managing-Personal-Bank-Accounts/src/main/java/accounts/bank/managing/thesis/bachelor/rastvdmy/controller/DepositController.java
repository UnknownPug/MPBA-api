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

/**
 * This class is responsible for handling deposit related requests.
 * It provides endpoints for getting all deposits, filtering deposits, getting a deposit by id,
 * opening a deposit, updating a deposit, and deleting a deposit.
 */
@Slf4j
@RestController
@RequestMapping(path = "/deposit")
public class DepositController {

    private static final Logger LOG = LoggerFactory.getLogger(DepositController.class);
    private final DepositService depositService;

    /**
     * Constructor for the DepositController.
     *
     * @param depositService The service to handle deposit operations.
     */
    @Autowired
    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    /**
     * This method is used to get all deposits.
     *
     * @return A list of all deposits.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<List<Deposit>> getAllDeposits() {
        LOG.info("Getting all deposits ...");
        return ResponseEntity.ok(depositService.getAllDeposits());
    }

    /**
     * This method is used to filter deposits.
     *
     * @param page The page number.
     * @param size The size of the page.
     * @param sort The sort order.
     * @return A page of filtered deposits.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter")
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

    /**
     * This method is used to get a deposit by id.
     *
     * @param depositId The id of the deposit.
     * @return The deposit with the given id.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Deposit> getDepositById(@PathVariable(value = "id") Long depositId) {
        LOG.info("Getting deposit id: {} ...", depositId);
        return ResponseEntity.ok(depositService.getDepositById(depositId));
    }

    /**
     * This method is used to open a deposit.
     *
     * @param request The request containing the card number, deposit amount, description, and currency.
     * @return The opened deposit.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Deposit> openDeposit(@RequestBody DepositRequest request) {
        LOG.info("Creating deposit ...");
        return ResponseEntity.ok(depositService.openDeposit(request.cardNumber(), request.depositAmount(), request.description(), request.currency()));
    }

    /**
     * This method is used to update a deposit.
     *
     * @param depositId The id of the deposit.
     * @param request   The request containing the card number, description, deposit amount, and currency.
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PutMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void updateDeposit(
            @PathVariable(value = "id") Long depositId,
            @RequestBody DepositRequest request) {
        LOG.info("Updating deposit ...");
        depositService.updateDeposit(depositId, request.cardNumber(), request.description(), request.depositAmount(), request.currency());
    }

    /**
     * This method is used to delete a deposit.
     *
     * @param depositId The id of the deposit.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public void deleteDeposit(@PathVariable(value = "id") Long depositId) {
        LOG.info("Deleting deposit ...");
        depositService.deleteDeposit(depositId);
    }
}
