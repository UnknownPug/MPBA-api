package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankLoanRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.TimeRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankLoan;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
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
        LOG.debug("Getting all loans ...");
        return ResponseEntity.ok(bankLoanService.getAllLoans());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    public ResponseEntity<BankLoan> getLoanById(@PathVariable(value = "id") Long loanId) {
        LOG.debug("Getting loan id: {} ...", loanId);
        return ResponseEntity.ok(bankLoanService.getLoanById(loanId));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/reference")
    public ResponseEntity<BankLoan> getLoanByReferenceNumber(String referenceNumber) {
        LOG.debug("Getting loan by reference: {} ...", referenceNumber);
        return ResponseEntity.ok(bankLoanService.getLoanByReferenceNumber(referenceNumber));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{id}")
    public ResponseEntity<BankLoan> openLoan(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "option") String option,
                                             @RequestBody BankLoanRequest loanRequest) {
        if (option.equals("settlement-account")) {
            LOG.debug("Opening settlement account for loan for user {} ...", id);
            return ResponseEntity.ok(bankLoanService.openSettlementAccount(
                    id, loanRequest.loanAmount(), loanRequest.currencyType()));
        } else if (option.equals("card")) {
            LOG.debug("Opening card for loan for user with card {} ...", id);
            return ResponseEntity.ok(bankLoanService.addLoanToCard(
                    id, loanRequest.loanAmount(), loanRequest.currencyType()));
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid type. Use 'settlement-account' or 'card'");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}")
    public void loanRepayment(@PathVariable(value = "id") Long loanId, @RequestBody BankLoanRequest loanRequest) {
        LOG.debug("Repaying settlement account loan {} ...", loanId);
        bankLoanService.repayLoan(loanId, loanRequest.loanAmount(), loanRequest.currencyType());
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/date")
    public void updateLoanDate(@PathVariable(value = "id") Long loanId, @RequestBody TimeRequest timeRequest) {
        LOG.debug("Updating settlement account loan {} ...", loanId);
        bankLoanService.updateLoanDate(loanId, timeRequest.startDate(), timeRequest.expirationDate());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void deleteLoan(@PathVariable(value = "id") Long loanId) {
        LOG.debug("Deleting settlement account loan for user {} ...", loanId);
        bankLoanService.deleteCardLoan(loanId);
    }
}
