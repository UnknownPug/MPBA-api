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
        LOG.debug("Getting all loans...");
        return ResponseEntity.ok(bankLoanService.getAllLoans());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    public ResponseEntity<BankLoan> getLoanById(@PathVariable(value = "id") Long loanId) {
        LOG.debug("Getting loan id: {} ...", loanId);
        return ResponseEntity.ok(bankLoanService.getLoanById(loanId));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{id}")
    public ResponseEntity<BankLoan> openLoan(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "type") String type,
                                             @RequestBody BankLoanRequest loanRequest) {
        if (type.equals("settlement-account")) {
            LOG.debug("Opening settlement account for loan for user {} ...", id);
            return ResponseEntity.ok(bankLoanService.openSettlementAccount(id, loanRequest.loanAmount()));
        } else if (type.equals("card")) {
            LOG.debug("Opening card for loan for user with card {} ...", id);
            return ResponseEntity.ok(bankLoanService.addLoanToCard(id, loanRequest.loanAmount()));
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid type. Use 'settlement-account' or 'card'");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}")
    public void loanRepayment(@PathVariable(value = "id") Long loanId,
                              @RequestParam(value = "type") String type,
                              @RequestBody BankLoanRequest loanRequest) {
        if (type.equals("settlement-account")) {
            LOG.debug("Repaying settlement account loan for user {} ...", loanId);
            bankLoanService.repaySettlementAccountLoan(loanId, loanRequest.loanAmount());
        } else if (type.equals("card")) {
            LOG.debug("Repaying card loan for user {} ...", loanId);
            bankLoanService.repayCardLoan(loanId, loanRequest.loanAmount());
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid type. Use 'settlement-account' or 'card'");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/date")
    public void updateLoanDate(@PathVariable(value = "id") Long loanId,
                               @RequestParam(value = "type") String type,
                               @RequestBody TimeRequest timeRequest) {
        if (type.equals("settlement-account")) {
            LOG.debug("Updating settlement account loan date for user {} ...", loanId);
            bankLoanService.updateSettlementAccountLoanDate(loanId,
                    timeRequest.startDate(),
                    timeRequest.expirationDate()
            );
        } else if (type.equals("card")) {
            LOG.debug("Updating card loan date for user {} ...", loanId);
            bankLoanService.updateCardLoanDate(loanId, timeRequest.startDate(), timeRequest.expirationDate());
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid type. Use 'settlement-account' or 'card'");
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    public void deleteLoan(@PathVariable(value = "id") Long loanId,
                           @RequestParam(value = "type") String type) {
        if (type.equals("settlement-account")) {
            LOG.debug("Deleting settlement account loan for user {} ...", loanId);
            bankLoanService.deleteSettlementAccountLoan(loanId);
        } else if (type.equals("card")) {
            LOG.debug("Deleting card loan for user {} ...", loanId);
            bankLoanService.deleteCardLoan(loanId);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid type. Use 'settlement-account' or 'card'");
        }
    }
}
