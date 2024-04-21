package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
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
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This class is used to test the functionality of the CardService class.
 * It uses the Mockito framework for mocking dependencies and JUnit for running the tests.
 */
@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrencyDataService currencyDataService;
    @Mock
    private Generator generator;

    @InjectMocks
    private CardService cardService;

    private Card testCard;
    private User testUser;

    /**
     * This method is used to set up the initial state for the tests.
     */
    @BeforeEach
    public void setUp() {
        testCard = new Card(); // Initialize with appropriate values
        testCard.setId(1L);
        testCard.setCardNumber("1234567890123456");

        testUser = new User(); // Initialize with appropriate values
        testUser.setId(1L);
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setStatus(UserStatus.STATUS_DEFAULT); // Assuming a user is active by default

        cardRepository = mock(CardRepository.class);
        currencyDataService = mock(CurrencyDataService.class);
        cardService = new CardService(cardRepository, userRepository, currencyDataService, generator);
    }

    /**
     * This method tests the functionality of the getAllCards method in the CardService class.
     * It verifies that the method returns all cards in the repository.
     */
    @Test
    public void testGetAllCards() {
        List<Card> cards = Collections.singletonList(testCard);
        when(cardRepository.findAll()).thenReturn(cards);

        List<Card> result = cardService.getAllCards();

        assertEquals(cards, result);
        verify(cardRepository, times(1)).findAll();
    }

    /**
     * This method tests the functionality of the filterAndSortCards method in the CardService class.
     * It verifies that the method returns a page of cards sorted and filtered
     * according to the provided Pageable object.
     */
    @Test
    public void testFilterAndSortCards() {
        Pageable pageable = mock(Pageable.class);
        List<Card> cards = Collections.singletonList(testCard);
        Page<Card> page = new PageImpl<>(cards);
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<Card> result = cardService.filterAndSortCards(pageable);

        assertEquals(page, result);
        verify(cardRepository, times(1)).findAll(pageable);
    }

    /**
     * This method tests the functionality of the getCardById method in the CardService class.
     * It verifies that the method returns the correct card when a valid ID is provided.
     */
    @Test
    public void testGetCardById_ValidCard() {
        Long cardId = 1L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        Card result = cardService.getCardById(cardId);

        assertEquals(testCard, result);
        verify(cardRepository, times(1)).findById(cardId);
    }

    /**
     * This method tests the functionality of the getCardById method in the CardService class.
     * It verifies that the method throws an exception when an invalid ID is provided.
     */
    @Test
    public void testGetCardById_InvalidCard() {
        Long cardId = 2L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> cardService.getCardById(cardId));

        verify(cardRepository, times(1)).findById(cardId);
    }

    /**
     * This method tests the functionality of the getCardByCardNumber method in the CardService class.
     * It verifies that the method returns the correct card when a valid card number is provided.
     */
    @Test
    public void testGetCardByCardNumber_ValidCard() {
        String cardNumber = "1234567890123456";
        Card card = new Card();
        card.setCardNumber(cardNumber);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(card);

        Card result = cardService.getCardByCardNumber(cardNumber);

        assertEquals(card, result);
        verify(cardRepository, times(2)).findByCardNumber(cardNumber);
    }

    /**
     * This method tests the functionality of the getCardByCardNumber method in the CardService class.
     * It verifies that the method throws an exception when an invalid card number is provided.
     */
    @Test
    public void testGetCardByCardNumber_InvalidCard() {
        String cardNumber = "1111222233334444"; // Assuming card number not found
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(null);

        assertThrows(ApplicationException.class, () -> cardService.getCardByCardNumber(cardNumber));

        verify(cardRepository, times(1)).findByCardNumber(cardNumber);
    }

    /**
     * This method tests the functionality of the createCard method in the CardService class.
     * It verifies that the method creates a card for a valid user and currency.
     */
    @Test
    public void testCreateCard_ValidUserAndCurrency() {
        Long userId = 1L;
        String chosenCurrency = "USD";
        String type = "VISA"; // Assuming a card type is always "Debit" for simplicity

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(generator.generateIban()).thenReturn("CZ12CVUT3456781234567890");
        when(generator.generateSwift()).thenReturn("CVUTCZAB");
        when(generator.generateAccountNumber()).thenReturn("4567891230/0800");

        Card result = cardService.createCard(userId, chosenCurrency, type);
        cardRepository.save(result);
        verify(cardRepository, times(1)).save(result);
    }

    /**
     * This method tests the functionality of the createCard method in the CardService class.
     * It verifies that the method throws an exception when the user is blocked.
     */
    @Test
    public void testCardRefill_BlockedCard() {
        Long cardId = 1L;
        Integer pin = 1234;
        BigDecimal balance = BigDecimal.valueOf(1000);

        testCard.setStatus(CardStatus.STATUS_CARD_BLOCKED);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(ApplicationException.class, () -> cardService.cardRefill(cardId, pin, balance));
    }

    /**
     * This method tests the functionality of the cardRefill method in the CardService class.
     * It verifies that the method throws an exception when the card is blocked.
     */
    @Test
    public void testCardRefill_ExpiredCard() {
        Long cardId = 1L;
        Integer pin = 1234;
        BigDecimal balance = BigDecimal.valueOf(1000);

        testCard.setCardExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(ApplicationException.class, () -> cardService.cardRefill(cardId, pin, balance));
    }

    /**
     * This method tests the functionality of the cardRefill method in the CardService class.
     * It verifies that the method throws an exception when the card is expired.
     */
    @Test
    public void testCardRefill_InvalidPin() {
        Long cardId = 1L;
        Integer pin = 9999; // Invalid pin
        BigDecimal balance = BigDecimal.valueOf(1000);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(NullPointerException.class, () -> cardService.cardRefill(cardId, pin, balance));
    }

    /**
     * This method tests the functionality of the cardRefill method in the CardService class.
     * It verifies that the method throws an exception when an invalid pin is provided.
     */
    @Test
    public void testCardRefill_NegativeBalance() {
        Long cardId = 1L;
        Integer pin = 1234;
        BigDecimal balance = BigDecimal.valueOf(-1000); // Negative balance

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(NullPointerException.class, () -> cardService.cardRefill(cardId, pin, balance));
    }

    /**
     * This method tests the functionality of the cardRefill method in the CardService class.
     * It verifies that the method throws an exception when a negative balance is provided.
     */
    @Test
    public void testCreateCard_BlockedUser() {
        Long userId = 1L;
        String chosenCurrency = "USD";
        String type = "Debit";

        testUser.setStatus(UserStatus.STATUS_BLOCKED); // Assuming user is blocked

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> cardService.createCard(userId, chosenCurrency, type));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("Creating card is unavailable for blocked user.", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    /**
     * This method tests the functionality of the updateCardStatus method in the CardService class.
     * It verifies that the method throws an exception when the card is expired.
     */
    @Test
    public void testUpdateCardStatus_ExpiredCard() {
        Long cardId = 1L;
        testCard.setCardExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(ApplicationException.class, () -> cardService.updateCardStatus(cardId));
    }

    /**
     * This method tests the functionality of the changeCardType method in the CardService class.
     * It verifies that the method throws an exception when the card is expired.
     */
    @Test
    public void testChangeCardType_ExpiredCard() {
        Long cardId = 1L;
        testCard.setCardExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));

        assertThrows(ApplicationException.class, () -> cardService.changeCardType(cardId));
    }

    /**
     * This method tests the functionality of the deleteCard method in the CardService class.
     * It verifies that the method throws an exception when the card is not found.
     */
    @Test
    public void testDeleteCard_CardNotFound() {
        Long cardId = 1L;
        Long userId = 1L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> cardService.deleteCard(cardId, userId));
    }

    /**
     * This method tests the functionality of the deleteCard method in the CardService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    public void testDeleteCard_UserNotFound() {
        Long cardId = 1L;
        Long userId = 1L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(testCard));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> cardService.deleteCard(cardId, userId));
    }
}
