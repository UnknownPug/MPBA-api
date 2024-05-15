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
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * This class is responsible for managing bank loans.
 * It is annotated with @Service to indicate that it's a Spring managed service.
 * It uses BankLoanRepository, UserRepository, CurrencyDataRepository, and CardRepository to interact with the database.
 * It also uses a Generator to generate reference numbers.
 */
@Service
public class BankLoanService {
    private final BankLoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CurrencyDataRepository currencyRepository;
    private final CardRepository cardRepository;

    private final Generator generator;

    /**
     * Constructs a new BankLoanService with the given repositories and generator.
     *
     * @param loanRepository     The BankLoanRepository to use.
     * @param userRepository     The UserRepository to use.
     * @param currencyRepository The CurrencyDataRepository to use.
     * @param cardRepository     The CardRepository to use.
     * @param generator          The Generator to use.
     */
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

    /**
     * Retrieves all loans.
     *
     * @return A list of all loans.
     */
    @Cacheable(value = "loans")
    public List<BankLoan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Retrieves loans with filtering and sorting.
     *
     * @param pageable The pagination information.
     * @return A page of loans.
     */
    @Cacheable(value = "loans")
    public Page<BankLoan> filterAndSortLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    /**
     * Retrieves a loan by its ID.
     *
     * @param loanId The ID of the loan to retrieve.
     * @return The retrieved loan.
     */
    @Cacheable(value = "loans", key = "#loanId")
    public BankLoan getLoanById(Long loanId) {
        return loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
    }

    /**
     * Retrieves a loan by its reference number.
     *
     * @param referenceNumber The reference number of the loan to retrieve.
     * @return The retrieved loan.
     */
    @Cacheable(value = "loans", key = "#referenceNumber")
    public BankLoan getLoanByReferenceNumber(String referenceNumber) {
        if (referenceNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND,
                    "Card with reference number " + referenceNumber + " not found.");
        }
        return loanRepository.findByReferenceNumber(referenceNumber);
    }

    /**
     * Opens a settlement account for a user.
     *
     * @param id                 The ID of the user.
     * @param bigDecimal         The amount of the loan.
     * @param chosenCurrencyType The currency type of the loan.
     * @return The created loan.
     */
    @Transactional
    @CacheEvict(value = {"loans", "users"}, allEntries = true)
    public BankLoan openSettlementAccount(Long id, BigDecimal bigDecimal, String chosenCurrencyType) {
        return createBankLoanForUser(id, bigDecimal, chosenCurrencyType);
    }

    /**
     * Adds a loan to a card.
     *
     * @param id                 The ID of the card.
     * @param bigDecimal         The amount of the loan.
     * @param chosenCurrencyType The currency type of the loan.
     * @return The created loan.
     */
    @Transactional
    @CacheEvict(value = {"loans", "users"}, allEntries = true)
    public BankLoan addLoanToCard(Long id, BigDecimal bigDecimal, String chosenCurrencyType) {
        return createBankLoanForCard(id, bigDecimal, chosenCurrencyType);
    }

    /**
     * Creates a bank loan for a user.
     *
     * @param userId             The ID of the user.
     * @param loanAmount         The amount of the loan.
     * @param chosenCurrencyType The currency type of the loan.
     * @return The created loan.
     * @throws ApplicationException if the user is not found, is blocked, already has a loan, or the loan range is invalid.
     */
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

    /**
     * Creates a bank loan for a card.
     *
     * @param cardId             The ID of the card.
     * @param loanAmount         The amount of the loan.
     * @param chosenCurrencyType The currency type of the loan.
     * @return The created loan.
     * @throws ApplicationException if the card is not found, is blocked, the user already has a loan, or the loan range is invalid.
     */
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

    /**
     * Checks if the user already has a loan.
     *
     * @param user The user to check.
     * @throws ApplicationException if the user already has a loan.
     */
    private void checkUserLoan(User user) {
        if (user.getBankLoan() != null || user.getCards().stream().anyMatch(card -> card.getCardLoan() != null)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "User already has a loan.");
        }
    }

    /**
     * Checks if the loan amount is within the valid range.
     *
     * @param loanAmount The amount of the loan.
     * @return True if the loan amount is greater than 0 and less than 1,000,000, false otherwise.
     */
    private boolean isValidLoanRange(BigDecimal loanAmount) {
        return loanAmount.compareTo(BigDecimal.ZERO) > 0 && loanAmount.compareTo(BigDecimal.valueOf(1000000)) < 0;
    }

    /**
     * Creates a bank loan.
     *
     * @param loanAmount         The amount of the loan.
     * @param chosenCurrencyType The currency type of the loan.
     * @param referenceNumber    The reference number of the loan.
     * @return The created loan.
     * @throws ApplicationException if the reference number is invalid or the currency is invalid.
     */
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
        loan.setReferenceNumber(HtmlUtils.htmlEscape(referenceNumber));
        try {
            Currency currencyType = Currency.valueOf(chosenCurrencyType.toUpperCase());
            loan.setCurrency(currencyType);
        } catch (ApplicationException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid currency: " + chosenCurrencyType);
        }
        return loanRepository.save(loan);
    }

    /**
     * Checks if the reference number is valid.
     *
     * @param referenceNumber The reference number to check.
     * @return True if the reference number is not null, not empty, and its length is less than or equal to 11, false otherwise.
     */
    private boolean isValidReferenceNumber(String referenceNumber) {
        return referenceNumber != null && !referenceNumber.isEmpty() && referenceNumber.length() <= 11;
    }

    /**
     * Repays a loan.
     *
     * @param loanId       The ID of the loan to repay.
     * @param loanRefund   The amount to repay.
     * @param currencyType The currency type of the repayment.
     */
    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public void repayLoan(Long loanId, BigDecimal loanRefund, String currencyType) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan is not found.")
        );
        if (loan.getLoanAmount().compareTo(loanRefund) < 0.00) {
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
            if (loan.getLoanAmount().compareTo(BigDecimal.ZERO) == 0.00) {
                if (loan.getUserLoan() != null) {
                    System.out.println("Deleting user loan");
                    deleteUserLoan(loanId);
                    return;
                } else if (loan.getCardLoan() != null) {
                    System.out.println("Deleting card loan");
                    deleteCardLoan(loanId);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid currency: " + currencyType);
        }
        loanRepository.save(loan);
    }

    /**
     * Updates the date of a loan.
     *
     * @param loanId         The ID of the loan to update.
     * @param startDate      The new start date.
     * @param expirationDate The new expiration date.
     */
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

    /**
     * Deletes a user's loan.
     *
     * @param loanId The ID of the loan to delete.
     */
    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public void deleteUserLoan(Long loanId) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan with id " + loanId + " is not found.")
        );
        User user = userRepository.findByBankLoanId(loanId);
        if (user == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "User with bank loan id " + loanId + " is not found.");
        }
        System.out.println("USER: Loan amount: " + loan.getLoanAmount());
        if (loan.getLoanAmount().compareTo(BigDecimal.ZERO) == 0.00) {
            user.setBankLoan(null);
            userRepository.save(user);
            loanRepository.delete(loan);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Loan is not repaid and has " + loan.getLoanAmount() + " " + loan.getCurrency() + " left.");
        }
    }

    /**
     * Deletes a card's loan.
     *
     * @param loanId The ID of the loan to delete.
     */
    @Transactional
    @CacheEvict(value = "loans", allEntries = true)
    public void deleteCardLoan(Long loanId) {
        BankLoan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Loan with id " + loanId + " is not found.")
        );
        Card card = cardRepository.findByCardLoanId(loanId);
        if (card == null) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Card with bank loan id " + loanId + " is not found.");
        }
        System.out.println("CARD: Loan amount: " + loan.getLoanAmount());
        if (loan.getLoanAmount().compareTo(BigDecimal.ZERO) == 0.00) {
            card.setCardLoan(null);
            cardRepository.save(card);
            loanRepository.delete(loan);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Loan is not repaid and has " + loan.getLoanAmount() + " " + loan.getCurrency() + " left.");
        }
    }
}
