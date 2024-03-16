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

    public BankLoan createLoan(BigDecimal loanAmount) {
        BankLoan bankLoan = new BankLoan();
        if (loanAmount == null || loanRange(loanAmount)) {
            throw new IllegalArgumentException("Bank loan is not valid");
        }
        bankLoan.setLoanAmount(loanAmount);
        bankLoan.setStartDate(LocalDateTime.now());
        bankLoan.setExpirationDate(LocalDateTime.now().plusYears(1));
        return loanRepository.save(bankLoan);
    }

    public void updateLoan(Long loanId, BigDecimal loanAmount) {
        BankLoan bankLoan = loanRepository.findById(loanId).orElseThrow(
                () -> new IllegalArgumentException("Loan is not found.")
        );
        if (loanAmount == null || loanRange(loanAmount) || !loanAmount.equals(bankLoan.getLoanAmount())) {
            throw new IllegalArgumentException("Bank loan is not valid");
        }
        bankLoan.setLoanAmount(loanAmount);
        loanRepository.save(bankLoan);
    }

    private boolean loanRange(BigDecimal loanAmount) {
        return loanAmount.compareTo(BigDecimal.ZERO) <= 0 && loanAmount.compareTo(BigDecimal.valueOf(1000000)) >= 0;
    }

    public void updateLoanDate(Long loanId, LocalDateTime dateTime, LocalDateTime expirationDateTime) {
        BankLoan bankLoan = loanRepository.findById(loanId).orElseThrow(
                () -> new IllegalArgumentException("Loan is not found.")
        );
        if (dateTime == null || expirationDateTime == null || dateTime.isAfter(expirationDateTime)) {
            throw new IllegalArgumentException("Date could not be set.");
        }
        bankLoan.setStartDate(dateTime);
        bankLoan.setExpirationDate(expirationDateTime);
        loanRepository.save(bankLoan);
    }

    public void deleteLoan(Long loanId) {
        loanRepository.findById(loanId).orElseThrow(
                () -> new IllegalArgumentException("Loan is not found.")
        );
        loanRepository.deleteById(loanId);
    }
}
