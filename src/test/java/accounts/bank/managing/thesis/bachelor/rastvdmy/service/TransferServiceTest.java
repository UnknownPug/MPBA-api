package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.CardStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Transfer;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

/**
 * This class is used to test the functionality of the TransferService class.
 * It uses the Mockito framework for mocking dependencies and JUnit for running the tests.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CurrencyDataService currencyDataService;
    @Mock
    private TransferService transferService;

    /**
     * This method is used to set up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        transferRepository = mock(TransferRepository.class);
        cardRepository = mock(CardRepository.class);
        currencyDataService = mock(CurrencyDataService.class);
        transferService = new TransferService(transferRepository, cardRepository, currencyDataService);
    }

    /**
     * This method tests the functionality of the getTransfers method in the TransferService class.
     * It verifies that the method returns all transfers in the repository.
     */
    @Test
    void testGetTransfers() {
        // Mocking data
        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer());
        when(transferRepository.findAll()).thenReturn(transfers);

        // Testing the method
        List<Transfer> result = transferService.getTransfers();

        // Assertions
        assertEquals(result.size(), 1); // Ensure one transfer is returned
    }

    /**
     * This method tests the functionality of the filterAndSortTransfers method in the TransferService class.
     * It verifies that the method returns a page of transfers sorted and filtered
     * according to the provided Pageable object.
     */
    @Test
    void testFilterAndSortTransfers() {
        // Mocking data
        Pageable pageable = Pageable.unpaged();
        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer());
        Page<Transfer> page = new PageImpl<>(transfers, pageable, transfers.size());
        when(transferRepository.findAll(pageable)).thenReturn(page);

        // Testing the method
        Page<Transfer> result = transferService.filterAndSortTransfers(pageable);

        // Assertions
        assertEquals(result.getTotalElements(), 1); // Ensure one transfer is returned
    }

    /**
     * This method tests the functionality of the getTransferById method in the TransferService class.
     * It verifies that the method returns the correct transfer when a valid ID is provided.
     */
    @Test
    void testGetTransferById_ExistingId() {
        // Mocking data
        Long transferId = 1L;
        Transfer transfer = new Transfer();
        transfer.setId(transferId);
        when(transferRepository.findById(transferId)).thenReturn(Optional.of(transfer));

        // Testing the method
        Transfer result = transferService.getTransferById(transferId);

        // Assertions
        assertEquals(result.getId(), transferId); // Ensure the correct transfer is returned
    }

    /**
     * This method tests the functionality of the getTransferById method in the TransferService class.
     * It verifies that the method throws an exception when an invalid ID is provided.
     */
    @Test
    void testGetTransferById_NonExistingId() {
        // Mocking data
        Long transferId = 1L;
        when(transferRepository.findById(transferId)).thenReturn(Optional.empty());

        // Testing the method and expecting an exception
        try {
            transferService.getTransferById(transferId);
        } catch (ApplicationException e) {
            // Assertions
            assertEquals(e.getHttpStatus(), HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the getTransferByReferenceNumber method in the TransferService class.
     * It verifies that the method returns the correct transfer when a valid reference number is provided.
     */
    @Test
    void testGetTransferByReferenceNumber_NonEmptyReferenceNumber() {
        // Mocking data
        String referenceNumber = "REF123";
        Transfer transfer = new Transfer();
        when(transferRepository.findByReferenceNumber(referenceNumber)).thenReturn(transfer);

        // Testing the method
        Transfer result = transferService.getTransferByReferenceNumber(referenceNumber);

        // Assertions
        assertNotEquals(result ,null); // Ensure a transfer is returned
    }

    /**
     * This method tests the functionality of the getTransferByReferenceNumber method in the TransferService class.
     * It verifies that the method throws an exception when an empty reference number is provided.
     */
    @Test
    void testGetTransferByReferenceNumber_EmptyReferenceNumber() {
        // Mocking data
        String referenceNumber = "";

        // Testing the method and expecting an exception
        try {
            transferService.getTransferByReferenceNumber(referenceNumber);
        } catch (ApplicationException e) {
            // Assertions
            assertEquals(e.getHttpStatus(), HttpStatus.BAD_REQUEST); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the createTransfer method in the TransferService class.
     * It verifies that the method throws an exception when the sender card is not found.
     */
    @Test
    public void testCreateTransfer_SenderCardNotFound() {
        Long senderId = 1L;
        String receiverCardNumber = "1234567890";
        BigDecimal amount = BigDecimal.valueOf(1000);
        String description = "Test Description";

        when(cardRepository.findById(senderId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> transferService.createTransfer(senderId, receiverCardNumber, amount, description));
    }

    /**
     * This method tests the functionality of the createTransfer method in the TransferService class.
     * It verifies that the method throws an exception when the sender card is blocked.
     */
    @Test
    public void testCreateTransfer_SenderCardBlocked() {
        Long senderId = 1L;
        String receiverCardNumber = "1234567890";
        BigDecimal amount = BigDecimal.valueOf(1000);
        String description = "Test Description";

        Card senderCard = mock(Card.class);
        when(senderCard.getStatus()).thenReturn(CardStatus.STATUS_CARD_BLOCKED);
        when(cardRepository.findById(senderId)).thenReturn(Optional.of(senderCard));

        assertThrows(ApplicationException.class, () -> transferService.createTransfer(senderId, receiverCardNumber, amount, description));
    }

    /**
     * This method tests the functionality of the createTransfer method in the TransferService class.
     * It verifies that the method throws an exception when the sender card has insufficient funds.
     */
    @Test
    public void testCreateTransfer_InsufficientFunds() {
        Long senderId = 1L;
        String receiverCardNumber = "1234567890";
        BigDecimal amount = BigDecimal.valueOf(1000);
        String description = "Test Description";

        Card senderCard = mock(Card.class);
        when(senderCard.getStatus()).thenReturn(CardStatus.STATUS_CARD_DEFAULT);
        when(senderCard.getBalance()).thenReturn(BigDecimal.valueOf(500));
        when(cardRepository.findById(senderId)).thenReturn(Optional.of(senderCard));

        assertThrows(ApplicationException.class, () -> transferService.createTransfer(senderId, receiverCardNumber, amount, description));
    }

    /**
     * This method tests the functionality of the createTransfer method in the TransferService class.
     * It verifies that the method throws an exception when a negative amount is provided.
     */
    @Test
    public void testCreateTransfer_NegativeAmount() {
        Long senderId = 1L;
        String receiverCardNumber = "1234567890";
        BigDecimal amount = BigDecimal.valueOf(-1000);
        String description = "Test Description";

        Card senderCard = mock(Card.class);
        when(senderCard.getStatus()).thenReturn(CardStatus.STATUS_CARD_DEFAULT);
        when(senderCard.getBalance()).thenReturn(BigDecimal.valueOf(2000));
        when(cardRepository.findById(senderId)).thenReturn(Optional.of(senderCard));

        assertThrows(ApplicationException.class, () -> transferService.createTransfer(senderId, receiverCardNumber, amount, description));
    }

    /**
     * This method tests the functionality of the createTransfer method in the TransferService class.
     * It verifies that the method throws an exception when the receiver card is not found.
     */
    @Test
    public void testCreateTransfer_ReceiverCardNotFound() {
        Long senderId = 1L;
        String receiverCardNumber = "1234567890";
        BigDecimal amount = BigDecimal.valueOf(1000);
        String description = "Test Description";

        Card senderCard = mock(Card.class);
        when(senderCard.getStatus()).thenReturn(CardStatus.STATUS_CARD_DEFAULT);
        when(senderCard.getBalance()).thenReturn(BigDecimal.valueOf(2000));
        when(cardRepository.findById(senderId)).thenReturn(Optional.of(senderCard));
        when(cardRepository.findByCardNumber(receiverCardNumber)).thenReturn(null);

        assertThrows(ApplicationException.class, () -> transferService.createTransfer(senderId, receiverCardNumber, amount, description));
    }

    /**
     * This method tests the functionality of the createTransfer method in the TransferService class.
     * It verifies that the method throws an exception when the receiver card is blocked.
     */
    @Test
    public void testCreateTransfer_ReceiverCardBlocked() {
        Long senderId = 1L;
        String receiverCardNumber = "1234567890";
        BigDecimal amount = BigDecimal.valueOf(1000);
        String description = "Test Description";

        Card senderCard = mock(Card.class);
        when(senderCard.getStatus()).thenReturn(CardStatus.STATUS_CARD_DEFAULT);
        when(senderCard.getBalance()).thenReturn(BigDecimal.valueOf(2000));
        when(cardRepository.findById(senderId)).thenReturn(Optional.of(senderCard));

        Card receiverCard = mock(Card.class);
        when(receiverCard.getStatus()).thenReturn(CardStatus.STATUS_CARD_BLOCKED);
        when(cardRepository.findByCardNumber(receiverCardNumber)).thenReturn(receiverCard);

        assertThrows(ApplicationException.class, () -> transferService.createTransfer(senderId, receiverCardNumber, amount, description));
    }
}

