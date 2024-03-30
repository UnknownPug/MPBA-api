package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.BankLoan;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankLoanRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BankLoanService {
    private final BankLoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CurrencyDataService currencyDataService;
    private final CardRepository cardRepository;

    private final Generator generator;

    @Autowired
    public BankLoanService(BankLoanRepository loanRepository, UserRepository userRepository,
                           CurrencyDataService currencyDataService, CardRepository cardRepository, Generator generator) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.currencyDataService = currencyDataService;
        this.cardRepository = cardRepository;
        this.generator = generator;
    }

    public List<BankLoan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Page<BankLoan> filterAndSortLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    public BankLoan getLoanById(Long loanId) {
        return loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
    }

    public BankLoan getLoanByReferenceNumber(String referenceNumber) {
        if (referenceNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card with reference number " + referenceNumber + " not found.");
        }
        return loanRepository.findByReferenceNumber(referenceNumber);
    }

    @Transactional
    public BankLoan openSettlementAccount(Long id, BigDecimal bigDecimal, String chosenCurrencyType) {
        return createBankLoanForUser(id, bigDecimal, chosenCurrencyType);
    }

    @Transactional
    public BankLoan addLoanToCard(Long id, BigDecimal bigDecimal, String chosenCurrencyType) {
        return createBankLoanForCard(id, bigDecimal, chosenCurrencyType);
    }

    public BankLoan createBankLoanForUser(Long userId, BigDecimal loanAmount, String chosenCurrencyType) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );
        checkUserLoan(user);
        if (isValidLoanRange(loanAmount)) {
            BankLoan loan = createBankLoan(loanAmount, chosenCurrencyType);
            user.setBankLoan(loan);
            loan.setUserLoan(user);
            userRepository.save(user);
            return loan;
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid loan amount.");
        }
    }

    public BankLoan createBankLoanForCard(Long cardId, BigDecimal loanAmount, String chosenCurrencyType) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card not found.")
        );
        checkUserLoan(card.getUser());
        if (isValidLoanRange(loanAmount)) {
            BankLoan loan = createBankLoan(loanAmount, chosenCurrencyType);
            card.setCardLoan(loan);
            loan.setCardLoan(card);
            cardRepository.save(card);
            return loan;
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid loan amount.");
        }
    }

    private void checkUserLoan(User user) {
        if (user.getBankLoan() != null || user.getCards().stream().anyMatch(card -> card.getCardLoan() != null)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "User already has a loan.");
        }
    }

    private boolean isValidLoanRange(BigDecimal loanAmount) {
        return loanAmount.compareTo(BigDecimal.ZERO) <= 0 && loanAmount.compareTo(BigDecimal.valueOf(1000000)) >= 0;
    }

    private BankLoan createBankLoan(BigDecimal loanAmount, String chosenCurrencyType) {
        BankLoan loan = new BankLoan();
        loan.setLoanAmount(loanAmount);
        loan.setRepaidLoan(BigDecimal.ZERO);
        loan.setStartDate(LocalDateTime.now());
        loan.setExpirationDate(LocalDateTime.now().plusYears(1));
        loan.setReferenceNumber(generator.generateReferenceNumber());
        try {
            Currency currencyType = Currency.valueOf(chosenCurrencyType.toUpperCase());
            loan.setCurrency(currencyType);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid currency: " + chosenCurrencyType);
        }
        return loanRepository.save(loan);
    }

    public void repayLoan(Long loanId, BigDecimal bigDecimal, String currencyType) {
        BigDecimal rate = BigDecimal.valueOf(currencyDataService.findByCurrency(currencyType).getRate());
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
        if (loan.getLoanAmount().equals(BigDecimal.ZERO)) {
            deleteCardLoan(loanId);
        }
        if (loan.getLoanAmount().compareTo(bigDecimal) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid loan amount.");
        }
        if (loan.getCurrency().toString().equals(currencyType)) {
            loan.setRepaidLoan(loan.getRepaidLoan().subtract(bigDecimal));
        } else {
            switch (currencyType) {
                case "CZK", "UAH", "EUR", "USD":
                    loan.setLoanAmount(loan.getLoanAmount().subtract(rate.multiply(bigDecimal)));
                    break;
                default:
                    throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid currency type.");
            }
        }
        loanRepository.save(loan);
    }

    public void updateLoanDate(Long loanId, LocalDateTime localDateTime, LocalDateTime localDateTime1) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
        if (localDateTime.isAfter(localDateTime1) || localDateTime.isBefore(LocalDateTime.now()) ||
                localDateTime1.isBefore(LocalDateTime.now()) || localDateTime1.isBefore(localDateTime) ||
                localDateTime.isEqual(localDateTime1) ||
                (localDateTime.isEqual(loan.getStartDate()) && localDateTime1.isEqual(loan.getExpirationDate()))) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid date range.");
        }
        loan.setStartDate(localDateTime);
        loan.setExpirationDate(localDateTime1);
        loanRepository.save(loan);
    }

    public void deleteCardLoan(Long loanId) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
        if (loan.getLoanAmount().equals(BigDecimal.ZERO)) {
            loanRepository.delete(loan);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Loan is not repaid.");
        }
    }
}
