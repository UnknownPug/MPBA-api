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
    public ResponseEntity<List<Transfer>> getTransfers() {
        LOG.debug("Getting all transfers ...");
        return ResponseEntity.ok(transferService.getTransfers());
    }

    @GetMapping(path = "/filter")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Page<Transfer>> filterTransfers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.debug("Filtering transfers ...");
        if (sort.equalsIgnoreCase("asc")) {
            LOG.debug("Sorting transfers by amount in ascending order ...");
            Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").ascending());
            return ResponseEntity.ok(transferService.filterAndSortTransfers(pageable));
        } else if (sort.equalsIgnoreCase("desc")) {
            LOG.debug("Sorting transfers by amount in descending order ...");
            Pageable pageable = PageRequest.of(page, size, Sort.by("dateTime").descending());
            return ResponseEntity.ok(transferService.filterAndSortTransfers(pageable));
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'");
        }
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Transfer> getTransferById(@PathVariable(value = "id") Long transferId) {
        LOG.debug("Getting transfer id: {} ...", transferId);
        return ResponseEntity.ok(transferService.getTransferById(transferId));
    }

    @GetMapping(path = "/reference")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Transfer> getTransferByReferenceNumber(String referenceNumber) {
        LOG.debug("Getting transfer by reference: {} ...", referenceNumber);
        return ResponseEntity.ok(transferService.getTransferByReferenceNumber(referenceNumber));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Transfer> createTransfer(@RequestBody TransferRequest transfer) {
        LOG.debug("Creating transfer ...");
        return ResponseEntity.ok(
                transferService.createTransfer(
                        transfer.senderId(),
                        transfer.receiverCardNumber(),
                        transfer.amount(),
                        transfer.description())
        );
    }
}
