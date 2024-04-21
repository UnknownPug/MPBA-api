package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.TransferRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Transfer;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.TransferService;
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
 * This class is responsible for handling transfer related requests.
 * It provides endpoints for getting all transfers, filtering transfers, getting a transfer by id or reference number,
 * and creating a transfer.
 */
@Slf4j
@RestController
@RequestMapping(path = "/transfer")
public class TransferController {
    private final static Logger LOG = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;

    /**
     * Constructor for the TransferController.
     *
     * @param transferService The service to handle transfer operations.
     */
    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * This method is used to get all transfers.
     *
     * @return A list of all transfers.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<List<Transfer>> getTransfers() {
        LOG.info("Getting all transfers ...");
        return ResponseEntity.ok(transferService.getTransfers());
    }

    /**
     * This method is used to filter transfers.
     *
     * @param page The page number.
     * @param size The size of the page.
     * @param sort The sort order.
     * @return A page of filtered transfers.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Page<Transfer>> filterTransfers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.info("Filtering transfers ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.info("Sorting transfers by amount in ascending order ...");
                Pageable pageableAsc = PageRequest.of(page, size, Sort.by("dateTime").ascending());
                return ResponseEntity.ok(transferService.filterAndSortTransfers(pageableAsc));
            case "desc":
                LOG.info("Sorting transfers by amount in descending order ...");
                Pageable pageableDesc = PageRequest.of(page, size, Sort.by("dateTime").descending());
                return ResponseEntity.ok(transferService.filterAndSortTransfers(pageableDesc));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    /**
     * This method is used to get a transfer by id.
     *
     * @param id The id of the transfer.
     * @return The transfer with the given id.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Transfer> getTransferById(@PathVariable(value = "id") Long id) {
        LOG.info("Getting transfer id: {} ...", id);
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    /**
     * This method is used to get a transfer by reference number.
     *
     * @param request The request containing the reference number.
     * @return The transfer with the given reference number.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/reference")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Transfer> getTransferByReferenceNumber(@RequestBody TransferRequest request) {
        LOG.info("Getting transfer by reference: {} ...", request.referenceNumber());
        return ResponseEntity.ok(transferService.getTransferByReferenceNumber(request.referenceNumber()));
    }

    /**
     * This method is used to create a transfer.
     *
     * @param transfer The request containing the sender id, receiver card number, amount, and description.
     * @return The created transfer.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Transfer> createTransfer(@RequestBody TransferRequest transfer) {
        LOG.info("Creating transfer ...");
        return ResponseEntity.ok(
                transferService.createTransfer(
                        transfer.senderId(),
                        transfer.receiverCardNumber(),
                        transfer.amount(),
                        transfer.description())
        );
    }
}
