package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankLoanRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BankLoanService {
    private final BankLoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CurrencyDataRepository currencyRepository;
    private final CardRepository cardRepository;

    private final Generator generator;

    @Autowired
    public BankLoanService(BankLoanRepository loanRepository, UserRepository userRepository,
                           CurrencyDataRepository currencyRepository, CardRepository cardRepository,
                           Generator generator) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.cardRepository = cardRepository;
        this.generator = generator;
    }

    @Cacheable(value = "loans")
    public List<BankLoan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Cacheable(value = "loans")
    public Page<BankLoan> filterAndSortLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    @Cacheable(value = "loans", key = "#loanId")
    public BankLoan getLoanById(Long loanId) {
        return loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
    }

    @Cacheable(value = "loans", key = "#referenceNumber")
    public BankLoan getLoanByReferenceNumber(String referenceNumber) {
        if (referenceNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND,
                    "Card with reference number " + referenceNumber + " not found.");
        }
        return loanRepository.findByReferenceNumber(referenceNumber);
    }

    @Transactional
    @CacheEvict(value = {"loans", "users"}, allEntries = true)
    public BankLoan openSettlementAccount(Long id, BigDecimal bigDecimal, String chosenCurrencyType) {
        return createBankLoanForUser(id, bigDecimal, chosenCurrencyType);
    }

    @Transactional
    @CacheEvict(value = {"loans", "users"}, allEntries = true)
    public BankLoan addLoanToCard(Long id, BigDecimal bigDecimal, String chosenCurrencyType) {
        return createBankLoanForCard(id, bigDecimal, chosenCurrencyType);
    }

    private BankLoan createBankLoanForUser(Long userId, BigDecimal loanAmount, String chosenCurrencyType) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable for blocked user.");
        }
        checkUserLoan(user);
        if (isValidLoanRange(loanAmount)) {
            BankLoan loan = createBankLoan(loanAmount, chosenCurrencyType, generator.generateReferenceNumber());
            user.setBankLoan(loan);
            loan.setUserLoan(user);
            userRepository.save(user);
            return loanRepository.save(loan);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Loan range is invalid.");
        }
    }

    private BankLoan createBankLoanForCard(Long cardId, BigDecimal loanAmount, String chosenCurrencyType) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Card not found.")
        );
        if (card.getStatus() == CardStatus.STATUS_CARD_BLOCKED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Operation is unavailable for blocked card.");
        }
        checkUserLoan(card.getUser());
        if (isValidLoanRange(loanAmount)) {
            BankLoan loan = createBankLoan(loanAmount, chosenCurrencyType, generator.generateReferenceNumber());
            card.setCardLoan(loan);
            loan.setCardLoan(card);
            cardRepository.save(card);
            return loanRepository.save(loan);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Loan range is invalid.");
        }
    }

    private void checkUserLoan(User user) {
        if (user.getBankLoan() != null || user.getCards().stream().anyMatch(card -> card.getCardLoan() != null)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "User already has a loan.");
        }
    }

    private boolean isValidLoanRange(BigDecimal loanAmount) {
        return loanAmount.compareTo(BigDecimal.ZERO) > 0 && loanAmount.compareTo(BigDecimal.valueOf(1000000)) < 0;
    }

    private BankLoan createBankLoan(BigDecimal loanAmount, String chosenCurrencyType, String referenceNumber) {
        BankLoan loan = new BankLoan();
        loan.setLoanAmount(loanAmount);
        loan.setRepaidLoan(BigDecimal.ZERO);
        loan.setStartDate(LocalDate.now());
        loan.setExpirationDate(LocalDate.now().plusYears(1));
        if (!isValidReferenceNumber(referenceNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid reference number. Size must be less or equal to 11.");
        }
        loan.setReferenceNumber(referenceNumber);
        try {
            Currency currencyType = Currency.valueOf(chosenCurrencyType.toUpperCase());
            loan.setCurrency(currencyType);
        } catch (ApplicationException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid currency: " + chosenCurrencyType);
        }
        return loanRepository.save(loan);
    }

    private boolean isValidReferenceNumber(String referenceNumber) {
        return referenceNumber != null && !referenceNumber.isEmpty() && referenceNumber.length() <= 11;
    }

    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public void repayLoan(Long loanId, BigDecimal loanRefund, String currencyType) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
        if (loan.getLoanAmount().compareTo(loanRefund) < 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid loan amount or loan was already repaid.");
        }
        try {
            Currency refundCurrency = Currency.valueOf(currencyType.toUpperCase());
            Currency loanCurrency = loan.getCurrency();

            if (refundCurrency != loanCurrency) {
                BigDecimal convertedAmount = loanRefund.multiply(
                        BigDecimal.valueOf(currencyRepository.findByCurrency(loanCurrency.toString()).getRate()));
                loan.setRepaidLoan(loan.getRepaidLoan().add(convertedAmount));
                loan.setLoanAmount(loan.getLoanAmount().subtract(convertedAmount));
            } else { // if the currency is the same
                loan.setRepaidLoan(loan.getRepaidLoan().add(loanRefund));
                loan.setLoanAmount(loan.getLoanAmount().subtract(loanRefund));
            }
            if (loan.getLoanAmount().compareTo(BigDecimal.ZERO) == 0) {
                deleteCardLoan(loanId);
                return;
            }
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid currency: " + currencyType);
        }
        loanRepository.save(loan);
    }

    @CacheEvict(value = "loans", allEntries = true)
    public void updateLoanDate(Long loanId, LocalDate startDate, LocalDate expirationDate) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
        if (startDate.isAfter(expirationDate) || expirationDate.isBefore(startDate) ||
                startDate.isBefore(LocalDate.now()) || expirationDate.isBefore(LocalDate.now())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid date range.");
        }
        loan.setStartDate(startDate);
        loan.setExpirationDate(expirationDate);
        loanRepository.save(loan);
    }

    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public void deleteBankLoan(Long loanId) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan with id " + loanId + " is not found.")
        );
        User user = userRepository.findByBankLoanId(loanId);
        deleteLoan(loanId, loan, user);
    }

    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public void deleteCardLoan(Long loanId) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan with id " + loanId + " is not found.")
        );
        User user = userRepository.findByCardLoanId(loanId);
        deleteLoan(loanId, loan, user);
    }

    private void deleteLoan(Long loanId, BankLoan loan, User user) {
        if (user == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "User with loan id " + loanId + " is not found.");
        }
        if (loan.getLoanAmount().compareTo(BigDecimal.ZERO) == 0) {
            user.setBankLoan(null);
            userRepository.save(user);
            loanRepository.deleteById(loanId); // now you can safely delete the loan
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Loan is not repaid and has " + loan.getLoanAmount() + " " + loan.getCurrency() + " left.");
        }
    }
}
