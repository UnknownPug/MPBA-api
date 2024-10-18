package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.*;
import api.mpba.rastvdmy.entity.enums.*;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.*;
import api.mpba.rastvdmy.service.impl.PaymentServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CurrencyDataService currencyDataService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private BankIdentityRepository bankIdentityRepository;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private BankAccount bankAccount;
    private BankAccount recipientAccount;
    private UserProfile userProfile;
    private Payment payment;
    private Card card;

    @BeforeEach
    void setUp() {
        userProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("2001-01-01")
                .countryOfOrigin("Czechia")
                .email("jhondoe@mpba.com")
                .password("Password123")
                .phoneNumber("+420123456789")
                .avatar("User.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();
        userProfileRepository.save(userProfile);

        BankIdentity bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName("CzechBank")
                .bankNumber("123456")
                .swift("CZBACZPP")
                .userProfile(userProfile)
                .build();
        bankIdentityRepository.save(bankIdentity);

        bankAccount = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(1000))
                .accountNumber("1234567890")
                .iban("CZ1234567890")
                .currency(Currency.CZK)
                .bankIdentity(bankIdentity)
                .build();

        recipientAccount = BankAccount.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(500))
                .accountNumber("0987654321")
                .iban("CZ0987654321")
                .currency(Currency.CZK)
                .bankIdentity(bankIdentity)
                .build();
        accountRepository.save(recipientAccount);

        card = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber("1234567890")
                .cvv("123")
                .pin("1234")
                .startDate(LocalDate.of(2021, 1, 1))
                .expirationDate(LocalDate.of(2025, 1, 1))
                .category(CardCategory.DEBIT)
                .type(CardType.VISA)
                .status(CardStatus.STATUS_CARD_DEFAULT)
                .account(bankAccount)
                .build();
        cardRepository.save(card);

        bankAccount.setCards(List.of(card));
        accountRepository.save(bankAccount);

        payment = Payment.builder()
                .id(UUID.randomUUID())
                .senderAccount(bankAccount)
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.CZK)
                .description("Test payment")
                .status(FinancialStatus.RECEIVED)
                .dateTime(LocalDate.now())
                .build();
    }

    @Test
    void getAllPayments_ShouldReturnPayments() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));
        when(paymentRepository.findAllBySenderAccountIdOrSenderCardId(bankAccount.getId(), List.of(card.getId())))
                .thenReturn(Optional.of(List.of(payment)));


        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(payment.getSenderName(), mockKey))
                    .thenReturn("John Doe");
            encryptionMock.when(() -> EncryptionUtil.decrypt(payment.getRecipientName(), mockKey))
                    .thenReturn("Jane Doe");
            encryptionMock.when(() -> EncryptionUtil.decrypt(payment.getDescription(), mockKey))
                    .thenReturn("Test payment");

            List<Payment> payments = paymentService
                    .getAllPayments(request, "CzechBank", bankAccount.getId());

            assertNotNull(payments);
            assertEquals(1, payments.size());
            verify(paymentRepository)
                    .findAllBySenderAccountIdOrSenderCardId(bankAccount.getId(), List.of(card.getId()));
        }
    }

    @Test
    void getAllPayments_ShouldThrowException_WhenBankNameDoesNotMatch() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        bankAccount.getBankIdentity().setBankName("DifferentBank");

        Exception exception = assertThrows(ApplicationException.class, () ->
                paymentService.getAllPayments(request, "CzechBank", bankAccount.getId()));

        assertEquals("Payments are not connected to the specified bank.", exception.getMessage());
    }

    @Test
    void getAllPayments_ShouldThrowException_WhenPaymentsNotFound() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(Collections.emptyList()));
        when(paymentRepository.findAllBySenderAccountIdOrSenderCardId(bankAccount.getId(), Collections.emptyList()))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(ApplicationException.class, () ->
                paymentService.getAllPayments(request, "CzechBank", bankAccount.getId()));

        assertEquals("Payments not found.", exception.getMessage());
    }

    @Test
    void getPaymentById_ShouldReturnPayment() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(paymentRepository.findCardIdByPaymentId(payment.getId())).thenReturn(Optional.of(card.getId()));
        when(paymentRepository.findBySenderCardIdAndId(card.getId(), payment.getId())).thenReturn(Optional.of(payment));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(payment.getSenderName(), mockKey))
                    .thenReturn("John Doe");
            encryptionMock.when(() -> EncryptionUtil.decrypt(payment.getRecipientName(), mockKey))
                    .thenReturn("Jane Doe");
            encryptionMock.when(() -> EncryptionUtil.decrypt(payment.getDescription(), mockKey))
                    .thenReturn("Test payment");

            Payment result = paymentService.getPaymentById(
                    request,
                    "CzechBank",
                    bankAccount.getId(),
                    payment.getId()
            );

            assertNotNull(result);
            assertEquals(payment.getId(), result.getId());
            verify(paymentRepository).findBySenderCardIdAndId(card.getId(), payment.getId());
        }
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenBankNameDoesNotMatch() {
        BankAccount mockBankAccount = mock(BankAccount.class);
        BankIdentity mockBankIdentity = mock(BankIdentity.class);
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(mockBankAccount));
        when(mockBankAccount.getBankIdentity()).thenReturn(mockBankIdentity);
        when(mockBankIdentity.getBankName()).thenReturn("DifferentBank");

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                paymentService.getPaymentById(request, "CzechBank", bankAccount.getId(), payment.getId()));

        assertEquals("Payment is not connected to the specified bank.", exception.getMessage());
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenPaymentNotFound() {
        BankAccount mockBankAccount = mock(BankAccount.class);
        BankIdentity mockBankIdentity = mock(BankIdentity.class);
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(mockBankAccount));
        when(mockBankAccount.getBankIdentity()).thenReturn(mockBankIdentity);
        when(mockBankIdentity.getBankName()).thenReturn("CzechBank");
        when(paymentRepository.findBySenderAccountIdAndId(eq(bankAccount.getId()), eq(payment.getId())))
                .thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                paymentService.getPaymentById(request, "CzechBank", bankAccount.getId(), payment.getId()));

        assertEquals("Payment not found.", exception.getMessage());
    }

    @Test
    void createBankTransfer_ShouldCreateAndReturnPayment() throws Exception {
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(accountRepository.findAll()).thenReturn(List.of(recipientAccount));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);
            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(recipientAccount.getAccountNumber(), mockKey))
                    .thenReturn(recipientAccount.getAccountNumber());
            encryptionMock.when(() -> EncryptionUtil.encrypt(anyString(), eq(mockKey)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Payment result = paymentService.createBankTransfer(
                    request,
                    bankAccount.getId(),
                    recipientAccount.getAccountNumber(),
                    BigDecimal.valueOf(100),
                    "Test payment"
            );

            assertNotNull(result);
            assertEquals(payment.getId(), result.getId());
            verify(paymentRepository, times(1)).save(any(Payment.class));
        }
    }

    @Test
    void createBankTransfer_ShouldThrowException_WhenSenderAccountNotFound() {
        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ApplicationException.class, () ->
                paymentService.createBankTransfer(
                        request,
                        UUID.randomUUID(),
                        "0987654321",
                        BigDecimal.valueOf(100),
                        "Test payment"
                )
        );
        assertEquals("Account not found.", exception.getMessage());
    }

    @Test
    void createBankTransfer_ShouldThrowException_WhenRecipientAccountNotFound() {
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(ApplicationException.class, () ->
                paymentService.createBankTransfer(
                        request,
                        bankAccount.getId(),
                        "0987654321",
                        BigDecimal.valueOf(100),
                        "Test payment"
                )
        );
        assertEquals("There are no accounts.", exception.getMessage());
    }

    @Test
    void createBankTransfer_ShouldThrowException_WhenAmountIsZero() {
        when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.of(bankAccount));
        when(accountRepository.findAll()).thenReturn(List.of(recipientAccount));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);
            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(recipientAccount.getAccountNumber(), mockKey))
                    .thenReturn(recipientAccount.getAccountNumber());
            encryptionMock.when(() -> EncryptionUtil.encrypt(anyString(), eq(mockKey)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Exception exception = assertThrows(ApplicationException.class, () ->
                    paymentService.createBankTransfer(
                            request,
                            bankAccount.getId(),
                            "0987654321",
                            BigDecimal.ZERO,
                            "Test payment"
                    )
            );

            assertEquals("Amount must be greater than zero.", exception.getMessage());
        }
    }

    @Test
    public void testCreateCardPayment_ValidCard_Success() throws Exception {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(cardRepository.findByAccountIdAndId(bankAccount.getId(), card.getId())).thenReturn(Optional.of(card));

        CurrencyData currencyData = new CurrencyData();
        currencyData.setRate(BigDecimal.valueOf(1));
        lenient().when(currencyDataService.convertCurrency(anyString(), anyString())).thenReturn(currencyData);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createCardPayment(request, bankAccount.getId(), card.getId());

        assertEquals(Currency.CZK, result.getCurrency());
        assertEquals(FinancialStatus.RECEIVED, result.getStatus());

        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    public void testCreateCardPayment_InvalidCard_Failure() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userProfile));
        card.setExpirationDate(LocalDate.now().minusDays(1));

        when(accountRepository.findById(bankAccount.getId())).thenReturn(Optional.of(bankAccount));
        when(cardRepository.findByAccountIdAndId(bankAccount.getId(), card.getId())).thenReturn(Optional.of(card));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                paymentService.createCardPayment(request, bankAccount.getId(), card.getId()));

        assertEquals("Operation is unavailable, card is unavailable to use.", exception.getMessage());

        verify(paymentRepository, never()).save(any(Payment.class));
    }
}