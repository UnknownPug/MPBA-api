package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.TransferRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Transfer;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.TransferService;
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
        LOG.info("Get all transfers");
        return ResponseEntity.ok(transferService.getTransfers());
    }

    @GetMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Transfer> getTransferById(@PathVariable(value = "id") Long transferId) {
        LOG.info("Get transfer by id: {}", transferId);
        return ResponseEntity.ok(transferService.getTransferById(transferId));
    }

    @PostMapping(path = "/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Transfer> createTransfer(@RequestBody TransferRequest transfer) {
        if (transfer.amount() == null || transfer.description() == null || transfer.commission() == null) {
            throw new IllegalArgumentException("Transfer data is not valid");
        }
        LOG.info("Create transfer...");
        return ResponseEntity.ok(
                transferService.createTransfer(transfer.amount(), transfer.description(), transfer.commission())
        );
    }

    @PutMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void updateTransfer(@PathVariable(value = "id") Long transferId, @RequestBody TransferRequest transfer) {
        if (transfer.amount() == null || transfer.description() == null || transfer.commission() == null) {
            throw new IllegalArgumentException("Transfer data is not valid");
        }
        LOG.info("Update transfer...");
        transferService.updateTransfer(transferId, transfer.amount(), transfer.description(), transfer.commission());
    }
}
