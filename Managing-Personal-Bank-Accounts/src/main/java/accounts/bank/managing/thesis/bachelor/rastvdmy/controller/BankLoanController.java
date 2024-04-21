package accounts.bank.managing.thesis.bachelor.rastvdmy.controller;

import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.BankLoanRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankLoan;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.BankLoanService;
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
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<List<BankLoan>> getAllLoans() {
        LOG.info("Getting all loans ...");
        return ResponseEntity.ok(bankLoanService.getAllLoans());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/filter")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<Page<BankLoan>> filterLoans(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "asc") String sort) {
        LOG.info("Filtering loans ...");
        switch (sort.toLowerCase()) {
            case "asc":
                LOG.info("Sorting loans by amount in ascending order ...");
                Pageable pageableAsc = PageRequest.of(page, size, Sort.by("loanAmount").ascending());
                return ResponseEntity.ok(bankLoanService.filterAndSortLoans(pageableAsc));
            case "desc":
                LOG.info("Sorting loans by amount in descending order ...");
                Pageable pageableDesc = PageRequest.of(page, size, Sort.by("loanAmount").descending());
                return ResponseEntity.ok(bankLoanService.filterAndSortLoans(pageableDesc));
            default:
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'asc' or 'desc'.");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<BankLoan> getLoanById(@PathVariable(value = "id") Long loanId) {
        LOG.info("Getting loan id: {} ...", loanId);
        return ResponseEntity.ok(bankLoanService.getLoanById(loanId));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/reference")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public ResponseEntity<BankLoan> getLoanByReferenceNumber(@RequestBody BankLoanRequest request) {
        LOG.info("Getting loan by reference: {} ...", request.referenceNumber());
        return ResponseEntity.ok(bankLoanService.getLoanByReferenceNumber(request.referenceNumber()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public ResponseEntity<BankLoan> openLoan(@PathVariable(value = "id") Long id,
                                             @RequestParam(value = "option") String option,
                                             @RequestBody BankLoanRequest loanRequest) {
        return switch (option.toLowerCase()) {
            case "user" -> {
                LOG.info("Opening settlement account for loan for user {} ...", id);
                yield ResponseEntity.ok(bankLoanService.openSettlementAccount(
                        id, loanRequest.loanAmount(), loanRequest.currencyType()));
            }
            case "card" -> {
                LOG.info("Opening card for loan for user with card {} ...", id);
                yield ResponseEntity.ok(bankLoanService.addLoanToCard(
                        id, loanRequest.loanAmount(), loanRequest.currencyType()));
            }
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid type. Use 'user' or 'card.'");
        };
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void loanRepayment(@PathVariable(value = "id") Long loanId, @RequestBody BankLoanRequest loanRequest) {
        LOG.info("Repaying settlement account loan {} ...", loanId);
        bankLoanService.repayLoan(loanId, loanRequest.loanAmount(), loanRequest.currencyType());
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PatchMapping(path = "/{id}/date")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public void updateLoanDate(@PathVariable(value = "id") Long loanId, @RequestBody BankLoanRequest request) {
        LOG.info("Updating settlement account loan {} ...", loanId);
        bankLoanService.updateLoanDate(loanId, request.startDate(), request.expirationDate());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_USER')")
    public void deleteLoan(@RequestParam String sort, @PathVariable(value = "id") Long loanId) {
        LOG.info("Deleting settlement account loan for user {} ...", loanId);
        switch (sort.toLowerCase()) {
            case "user" -> bankLoanService.deleteUserLoan(loanId);
            case "card" -> bankLoanService.deleteCardLoan(loanId);
            default -> throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid sort option. Use 'user' or 'card'.");
        }
    }
}
