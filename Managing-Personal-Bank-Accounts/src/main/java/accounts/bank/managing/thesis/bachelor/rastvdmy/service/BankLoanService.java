package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankLoan;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BankLoanService {
    private final BankLoanRepository loanRepository;

    @Autowired
    public BankLoanService(BankLoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<BankLoan> getAllLoans() {
        return loanRepository.findAll();
    }

    public BankLoan getLoanById(Long loanId) {
        return loanRepository.findById(loanId).orElseThrow(
                () -> new IllegalArgumentException("Loan is not found.")
        );
    }

    public BankLoan openSettlementAccount(Long id, BigDecimal bigDecimal) {
        // TODO: complete this method
        return null;
    }

    public BankLoan addLoanToCard(Long id, BigDecimal bigDecimal) {
        // TODO: complete this method
        return null;
    }

    public void repaySettlementAccountLoan(Long loanId, BigDecimal bigDecimal) {
        // TODO: complete this method
    }

    public void repayCardLoan(Long loanId, BigDecimal bigDecimal) {
        // TODO: complete this method
    }

    private boolean loanRange(BigDecimal loanAmount) {
        return loanAmount.compareTo(BigDecimal.ZERO) <= 0 && loanAmount.compareTo(BigDecimal.valueOf(1000000)) >= 0;
    }

    public void updateSettlementAccountLoanDate(Long loanId, LocalDateTime localDateTime, LocalDateTime localDateTime1) {
        // TODO: complete this method
    }

    public void updateCardLoanDate(Long loanId, LocalDateTime localDateTime, LocalDateTime localDateTime1) {
        // TODO: complete this method
    }

    public void deleteSettlementAccountLoan(Long loanId) {
        // TODO: complete this method
    }

    public void deleteCardLoan(Long loanId) {
        // TODO: complete this method
    }
}
