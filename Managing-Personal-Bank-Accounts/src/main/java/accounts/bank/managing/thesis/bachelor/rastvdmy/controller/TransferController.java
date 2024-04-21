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

@Slf4j
@RestController
@RequestMapping(path = "/transfer")
public class TransferController {
    private final static Logger LOG = LoggerFactory.getLogger(TransferController.class);
    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping(path = "/")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<List<Transfer>> getTransfers() {
        LOG.info("Getting all transfers ...");
        return ResponseEntity.ok(transferService.getTransfers());
    }

    @GetMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
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

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<Transfer> getTransferById(@PathVariable(value = "id") Long id) {
        LOG.info("Getting transfer id: {} ...", id);
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @GetMapping(path = "/reference")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Transfer> getTransferByReferenceNumber(@RequestBody TransferRequest request) {
        LOG.info("Getting transfer by reference: {} ...", request.referenceNumber());
        return ResponseEntity.ok(transferService.getTransferByReferenceNumber(request.referenceNumber()));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
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
