package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankLoanRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.TimeRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankLoan;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankLoanService;
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
@RequestMapping(path = "/loan")
public class BankLoanController {
    private static final Logger LOG = LoggerFactory.getLogger(BankLoanController.class);
    private final BankLoanService bankLoanService;

    @Autowired
    public BankLoanController(BankLoanService bankLoanService) {
        this.bankLoanService = bankLoanService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/")
    public ResponseEntity<List<BankLoan>> getAllLoans() {
        LOG.info("Get all loans");
        return ResponseEntity.ok(bankLoanService.getAllLoans());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    public ResponseEntity<BankLoan> getLoanById(@PathVariable(value = "id") Long loanId) {
        LOG.info("Get loan by id: {}", loanId);
        return ResponseEntity.ok(bankLoanService.getLoanById(loanId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/")
    public ResponseEntity<BankLoan> addLoanToUser(@RequestBody BankLoanRequest loanRequest) {
        LOG.info("Creating loan");
        return ResponseEntity.ok(bankLoanService.createLoan(loanRequest.loanAmount()));
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}")
    public void updateLoan(@PathVariable(value = "id") Long loanId, @RequestBody BankLoanRequest loanRequest) {
        LOG.info("Updating loan by id: {}", loanId);
        bankLoanService.updateLoan(loanId, loanRequest.loanAmount());
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(path = "/{id}/date")
    public void updateLoanDate(@PathVariable(value = "id") Long loanId, @RequestBody TimeRequest timeRequest) {
        LOG.info("Updating loan date by id: {}", loanId);
        bankLoanService.updateLoanDate(loanId, timeRequest.startDate(), timeRequest.expirationDate());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void deleteLoan(@PathVariable(value = "id") Long loanId) {
        LOG.info("Deleting loan by id: {}", loanId);
        bankLoanService.deleteLoan(loanId);
    }
}
