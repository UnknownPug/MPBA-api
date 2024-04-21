package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Card;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Currency;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Deposit;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CardRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.DepositRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;


@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private DepositRepository depositRepository;
    @Mock
    private Generator generator;
    @Mock
    private CurrencyDataRepository currencyRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private DepositService depositService;

    @BeforeEach
    void setUp() {
        depositRepository = mock(DepositRepository.class);
        generator = mock(Generator.class);
        currencyRepository = mock(CurrencyDataRepository.class);
        cardRepository = mock(CardRepository.class);
        depositService = new DepositService(depositRepository, generator, currencyRepository, cardRepository);
    }

    @Test
    void testGetAllDeposits() {
        // Mocking data
        List<Deposit> deposits = new ArrayList<>();
        deposits.add(new Deposit());
        when(depositRepository.findAll()).thenReturn(deposits);

        // Testing the method
        List<Deposit> result = depositService.getAllDeposits();

        // Assertions
        assert result.size() == 1; // Ensure one deposit is returned
    }

    @Test
    void testFilterAndSortDeposits() {
        // Mocking data
        Pageable pageable = Pageable.unpaged();
        List<Deposit> deposits = new ArrayList<>();
        deposits.add(new Deposit());
        Page<Deposit> page = new PageImpl<>(deposits, pageable, deposits.size());
        when(depositRepository.findAll(pageable)).thenReturn(page);

        // Testing the method
        Page<Deposit> result = depositService.filterAndSortDeposits(pageable);

        // Assertions
        assert result.getTotalElements() == 1; // Ensure one deposit is returned
    }

    @Test
    void testGetDepositById_ExistingId() {
        // Mocking data
        Long id = 1L;
        Deposit deposit = new Deposit();
        deposit.setId(id);
        when(depositRepository.findById(id)).thenReturn(Optional.of(deposit));

        // Testing the method
        Deposit result = depositService.getDepositById(id);

        // Assertions
        assert result.getId().equals(id); // Ensure the correct deposit is returned
    }

    @Test
    void testGetDepositById_NonExistingId() {
        // Mocking data
        Long id = 1L;
        when(depositRepository.findById(id)).thenReturn(Optional.empty());

        // Testing the method and expecting an exception
        try {
            depositService.getDepositById(id);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    @Test
    public void testDeleteDeposit_DepositNotFound() {
        Long depositId = 1L;

        when(depositRepository.findById(depositId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> depositService.deleteDeposit(depositId));
    }

    @Test
    public void testUpdateDeposit_DepositNotFound() {
        Long depositId = 1L;
        String cardNumber = "1234567890";
        String description = "Test Description";
        BigDecimal newAmount = BigDecimal.valueOf(1000);
        Currency currency = Currency.USD;

        when(depositRepository.findById(depositId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> depositService.updateDeposit(depositId, cardNumber, description, newAmount, currency));
    }

    @Test
    public void testOpenDeposit_CardNotFound() {
        String cardNumber = "1234567890";
        BigDecimal depositAmount = BigDecimal.valueOf(1000);
        String description = "Test Description";
        Currency currency = Currency.USD;

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(null);

        assertThrows(ApplicationException.class, () -> depositService.openDeposit(cardNumber, depositAmount, description, currency));
    }

    @Test
    public void testOpenDeposit_InvalidDepositAmount() {
        String cardNumber = "1234567890";
        BigDecimal depositAmount = BigDecimal.valueOf(0);
        String description = "Test Description";
        Currency currency = Currency.USD;

        assertThrows(ApplicationException.class, () -> depositService.openDeposit(cardNumber, depositAmount, description, currency));
    }

    @Test
    public void testOpenDeposit_InvalidDescription() {
        String cardNumber = "1234567890";
        BigDecimal depositAmount = BigDecimal.valueOf(1000);
        String description = "";
        Currency currency = Currency.USD;

        assertThrows(ApplicationException.class, () -> depositService.openDeposit(cardNumber, depositAmount, description, currency));
    }

    @Test
    void testUpdateDeposit_InvalidCard() {
        // Mocking data
        Long depositId = 1L;
        String cardNumber = "1234567890";
        BigDecimal newAmount = BigDecimal.valueOf(200);
        Currency currency = Currency.USD;

        Deposit deposit = new Deposit();
        deposit.setId(depositId);
        Card card = new Card();
        card.setCardNumber(cardNumber);
        card.setBalance(BigDecimal.valueOf(100));
        when(depositRepository.findById(depositId)).thenReturn(java.util.Optional.of(deposit));
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(card);

        // Testing the method and expecting an exception
        try {
            depositService.updateDeposit(depositId, cardNumber, "Updated deposit", newAmount, currency);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.BAD_REQUEST); // Ensure correct exception is thrown
        }
    }

    @Test
    public void testDeleteDeposit_DepositExpired() {
        Long depositId = 1L;
        Deposit deposit = mock(Deposit.class);
        Card card = mock(Card.class);
        when(deposit.getCardDeposit()).thenReturn(card);
        when(deposit.getExpirationDate()).thenReturn(LocalDateTime.now().minusDays(1));
        when(deposit.getDepositAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(deposit.getCurrency()).thenReturn(Currency.USD);
        when(card.getCurrencyType()).thenReturn(Currency.USD);
        when(card.getBalance()).thenReturn(BigDecimal.valueOf(2000));
        when(depositRepository.findById(depositId)).thenReturn(Optional.of(deposit));

        depositService.deleteDeposit(depositId);

        verify(cardRepository, times(1)).save(card);
        verify(depositRepository, times(1)).delete(deposit);
    }

    @Test
    public void testDeleteDeposit_DepositNotExpired() {
        Long depositId = 1L;
        Deposit deposit = mock(Deposit.class);
        Card card = mock(Card.class);
        when(deposit.getCardDeposit()).thenReturn(card);
        when(deposit.getExpirationDate()).thenReturn(LocalDateTime.now().plusDays(1));
        when(deposit.getDepositAmount()).thenReturn(BigDecimal.valueOf(1000));
        when(deposit.getCurrency()).thenReturn(Currency.USD);
        when(card.getCurrencyType()).thenReturn(Currency.USD);
        when(card.getBalance()).thenReturn(BigDecimal.valueOf(2000));
        when(depositRepository.findById(depositId)).thenReturn(Optional.of(deposit));

        depositService.deleteDeposit(depositId);

        verify(cardRepository, times(1)).save(card);
        verify(depositRepository, times(1)).delete(deposit);
    }
}
