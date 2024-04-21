package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.BankLoanRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class is used to test the functionality of the BankLoanService class.
 * It uses the Mockito framework for mocking dependencies and JUnit for running the tests.
 */
@ExtendWith(MockitoExtension.class)
public class BankLoanServiceTest {

    @Mock
    private BankLoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private Generator generator;

    @InjectMocks
    private BankLoanService bankLoanService;

    private BankLoan testLoan;
    private User testUser;
    private Card testCard;

    /**
     * This method is used to set up the test environment before each test method is executed.
     * It initializes the test objects with appropriate values.
     */
    @BeforeEach
    public void setUp() {
        testLoan = new BankLoan(); // Initialize with appropriate values
        testUser = new User(); // Initialize with appropriate values
        testCard = new Card(); // Initialize with appropriate values
    }

    /**
     * This method tests the functionality of the getAllLoans method in the BankLoanService class.
     * It verifies that the method returns all loans in the repository.
     */
    @Test
    public void testGetAllLoans() {
        List<BankLoan> loanList = new ArrayList<>();
        loanList.add(testLoan);

        when(loanRepository.findAll()).thenReturn(loanList);

        List<BankLoan> result = bankLoanService.getAllLoans();

        assertEquals(1, result.size());
        assertEquals(testLoan, result.get(0));

        verify(loanRepository, times(1)).findAll();
    }

    /**
     * This method tests the functionality of the filterAndSortLoans method in the BankLoanService class.
     * It verifies
     * that the method returns a page of loans sorted and filtered according to the provided Pageable object.
     */
    @Test
    public void testFilterAndSortLoans() {
        Pageable pageable = mock(Pageable.class);
        Page<BankLoan> page = new PageImpl<>(List.of(testLoan));

        when(loanRepository.findAll(pageable)).thenReturn(page);

        Page<BankLoan> result = bankLoanService.filterAndSortLoans(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(testLoan, result.getContent().get(0));

        verify(loanRepository, times(1)).findAll(pageable);
    }

    /**
     * This method tests the functionality of the getLoanById method in the BankLoanService class.
     * It verifies that the method returns the correct loan when a valid ID is provided.
     */
    @Test
    public void testGetLoanById_Exists() {
        Long loanId = 1L;

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        BankLoan result = bankLoanService.getLoanById(loanId);

        assertEquals(testLoan, result);

        verify(loanRepository, times(1)).findById(loanId);
    }

    /**
     * This method tests the functionality of the getLoanById method in the BankLoanService class.
     * It verifies that the method throws an exception when an invalid ID is provided.
     */
    @Test
    public void testGetLoanById_NotFound() {
        Long loanId = 1L;

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Assertions for exception handling can vary based on your implementation
        // Here, let's assume ApplicationException is thrown
        assertThrows(ApplicationException.class, () -> bankLoanService.getLoanById(loanId));

        verify(loanRepository, times(1)).findById(loanId);
    }

    /**
     * This method tests the functionality of the getLoanByReferenceNumber method in the BankLoanService class.
     * It verifies that the method returns the correct loan when a valid reference number is provided.
     */
    @Test
    public void testGetLoanByReferenceNumber_Success() {
        String referenceNumber = "123456789";
        when(loanRepository.findByReferenceNumber(referenceNumber)).thenReturn(testLoan);

        BankLoan result = bankLoanService.getLoanByReferenceNumber(referenceNumber);

        assertEquals(testLoan, result);
        verify(loanRepository, times(1)).findByReferenceNumber(referenceNumber);
    }

    /**
     * This method tests the functionality of the getLoanByReferenceNumber method in the BankLoanService class.
     * It verifies that the method throws an exception when an invalid reference number is provided.
     */
    @Test
    public void testGetLoanByReferenceNumber_NotFound() {
        String referenceNumber = ""; // Empty reference number
        assertThrows(ApplicationException.class, () -> bankLoanService.getLoanByReferenceNumber(referenceNumber));

        verify(loanRepository, never()).findByReferenceNumber(referenceNumber);
    }

    /**
     * This method tests the functionality of the addLoanToCard method in the BankLoanService class.
     * It verifies that the method throws an exception when an invalid card ID is provided.
     */
    @Test
    public void testAddLoanToCard_InvalidCard() {
        Long cardId = 1L; // Assuming a card with ID 1 does not exist
        BigDecimal loanAmount = BigDecimal.valueOf(50000);
        String chosenCurrencyType = "USD";

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> bankLoanService.addLoanToCard(cardId, loanAmount, chosenCurrencyType));

        verify(cardRepository, times(1)).findById(cardId);
        verify(generator, never()).generateReferenceNumber();
        verify(cardRepository, never()).save(any(Card.class));
        verify(loanRepository, never()).save(any(BankLoan.class));
    }

    /**
     * This method tests the functionality of the addLoanToCard method in the BankLoanService class.
     * It verifies that the method throws an exception when a blocked card ID is provided.
     */
    @Test
    public void testAddLoanToCard_BlockedCard() {
        Long cardId = 1L;
        BigDecimal loanAmount = BigDecimal.valueOf(50000);
        String chosenCurrencyType = "USD";

        testCard.setStatus(CardStatus.STATUS_CARD_BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(ApplicationException.class, () -> bankLoanService.addLoanToCard(cardId, loanAmount, chosenCurrencyType));

        verify(cardRepository, times(1)).findById(cardId);
        verify(generator, never()).generateReferenceNumber();
        verify(cardRepository, never()).save(any(Card.class));
        verify(loanRepository, never()).save(any(BankLoan.class));
    }

    /**
     * This method tests the functionality of the openSettlementAccount method in the BankLoanService class.
     * It verifies that the method throws an exception when an invalid user ID is provided.
     */
    @Test
    public void testOpenSettlementAccount_InvalidUser() {
        Long userId = 1L; // Assuming a user with ID 1 does not exist
        BigDecimal loanAmount = BigDecimal.valueOf(50000);
        String chosenCurrencyType = "USD";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> bankLoanService.openSettlementAccount(userId, loanAmount, chosenCurrencyType));

        verify(userRepository, times(1)).findById(userId);
        verify(generator, never()).generateReferenceNumber();
        verify(userRepository, never()).save(any(User.class));
        verify(loanRepository, never()).save(any(BankLoan.class));
    }

    /**
     * This method tests the functionality of the openSettlementAccount method in the BankLoanService class.
     * It verifies that the method throws an exception when an invalid loan range is provided.
     */
    @Test
    public void testOpenSettlementAccount_InvalidLoanRange() {
        Long userId = 1L;
        BigDecimal loanAmount = BigDecimal.valueOf(2000000); // Loan amount exceeds the maximum allowed
        String chosenCurrencyType = "USD";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThrows(ApplicationException.class, () -> bankLoanService.openSettlementAccount(userId, loanAmount, chosenCurrencyType));

        verify(userRepository, times(1)).findById(userId);
        verify(generator, never()).generateReferenceNumber();
        verify(userRepository, never()).save(any(User.class));
        verify(loanRepository, never()).save(any(BankLoan.class));
    }

    /**
     * This method tests the functionality of the openSettlementAccount method in the BankLoanService class.
     * It verifies that the method throws an exception when a blocked user ID is provided.
     */
    @Test
    public void testOpenSettlementAccount_BlockedUser() {
        Long userId = 1L;
        BigDecimal loanAmount = BigDecimal.valueOf(50000);
        String chosenCurrencyType = "USD";

        testUser.setStatus(UserStatus.STATUS_BLOCKED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        assertThrows(ApplicationException.class, () -> bankLoanService.openSettlementAccount(userId, loanAmount, chosenCurrencyType));

        verify(userRepository, times(1)).findById(userId);
        verify(generator, never()).generateReferenceNumber();
        verify(userRepository, never()).save(any(User.class));
        verify(loanRepository, never()).save(any(BankLoan.class));
    }

    /**
     * This method tests the functionality of the repayLoan method in the BankLoanService class.
     * It verifies that the method correctly updates the loan amount and repaid loan amount.
     */
    @Test
    public void testRepayLoan_Success() {
        Long loanId = 1L;
        BigDecimal loanRefund = BigDecimal.valueOf(500);
        String currencyType = "USD";

        BankLoan testLoan = new BankLoan();
        testLoan.setLoanAmount(BigDecimal.valueOf(1000));
        testLoan.setRepaidLoan(BigDecimal.ZERO);
        testLoan.setCurrency(Currency.valueOf(currencyType));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        bankLoanService.repayLoan(loanId, loanRefund, currencyType);

        assertEquals(BigDecimal.valueOf(500), testLoan.getLoanAmount());
        assertEquals(BigDecimal.valueOf(500), testLoan.getRepaidLoan());

        verify(loanRepository, times(1)).findById(loanId);
        verify(loanRepository, times(1)).save(testLoan);
    }

    /**
     * This method tests the functionality of the updateLoanDate method in the BankLoanService class.
     * It verifies that the method correctly updates the start and expiration dates of the loan.
     */
    @Test
    public void testUpdateLoanDate_Success() {
        Long loanId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate expirationDate = LocalDate.now().plusDays(10);

        BankLoan testLoan = new BankLoan();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        bankLoanService.updateLoanDate(loanId, startDate, expirationDate);

        assertEquals(startDate, testLoan.getStartDate());
        assertEquals(expirationDate, testLoan.getExpirationDate());

        verify(loanRepository, times(1)).findById(loanId);
        verify(loanRepository, times(1)).save(testLoan);
    }

    /**
     * This method tests the functionality of the updateLoanDate method in the BankLoanService class.
     * It verifies that the method throws an exception when an invalid date range is provided.
     */
    @Test
    public void testUpdateLoanDate_InvalidDateRange() {
        Long loanId = 1L;
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate expirationDate = LocalDate.now().plusDays(1);

        BankLoan testLoan = new BankLoan();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        assertThrows(ApplicationException.class, () -> bankLoanService.updateLoanDate(loanId, startDate, expirationDate));

        verify(loanRepository, times(1)).findById(loanId);
        verify(loanRepository, never()).save(testLoan);
    }

    /**
     * This method tests the functionality of the deleteUserLoan method in the BankLoanService class.
     * It verifies that the method correctly deletes a user's loan when the loan is fully repaid.
     */
    @Test
    public void testDeleteUserLoan_Success() {
        Long loanId = 1L;

        BankLoan testLoan = new BankLoan();
        testLoan.setLoanAmount(BigDecimal.ZERO);
        User testUser = new User();
        testUser.setBankLoan(testLoan);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(userRepository.findByBankLoanId(loanId)).thenReturn(testUser);

        bankLoanService.deleteUserLoan(loanId);

        assertNull(testUser.getBankLoan());

        verify(loanRepository, times(1)).findById(loanId);
        verify(userRepository, times(1)).findByBankLoanId(loanId);
        verify(userRepository, times(1)).save(testUser);
        verify(loanRepository, times(1)).delete(testLoan);
    }

    /**
     * This method tests the functionality of the deleteUserLoan method in the BankLoanService class.
     * It verifies that the method throws an exception when the loan is not fully repaid.
     */
    @Test
    public void testDeleteUserLoan_LoanNotFullyRepaid() {
        Long loanId = 1L;

        BankLoan testLoan = new BankLoan();
        testLoan.setLoanAmount(BigDecimal.valueOf(500));
        User testUser = new User();
        testUser.setBankLoan(testLoan);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        when(userRepository.findByBankLoanId(loanId)).thenReturn(testUser);

        assertThrows(ApplicationException.class, () -> bankLoanService.deleteUserLoan(loanId));

        assertNotNull(testUser.getBankLoan());

        verify(loanRepository, times(1)).findById(loanId);
        verify(userRepository, times(1)).findByBankLoanId(loanId);
        verify(userRepository, never()).save(testUser);
        verify(loanRepository, never()).delete(testLoan);
    }
}
