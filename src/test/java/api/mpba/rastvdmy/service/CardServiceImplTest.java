package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.*;
import api.mpba.rastvdmy.entity.enums.*;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.*;
import api.mpba.rastvdmy.service.impl.CardServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BankIdentityRepository bankIdentityRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private HttpServletRequest request;

    private UserProfile userProfile;
    private BankIdentity bankIdentity;
    private BankAccount bankAccount;
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

        bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName("CzechBank")
                .bankNumber("123456")
                .swift("CZBACZPP")
                .userProfile(userProfile)
                .bankAccounts(List.of())
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
        bankAccountRepository.save(bankAccount);
    }

    @Test
    void getAccountCards_ShouldReturnCards_WhenCardsExist() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(card.getCardNumber(), mockKey))
                    .thenReturn("1234567890");
            encryptionMock.when(() -> EncryptionUtil.decrypt(card.getCvv(), mockKey)).thenReturn("123");

            List<Card> cards = cardService.getAccountCards("bankName", bankAccount.getId(), request);

            assertNotNull(cards);
            assertEquals(1, cards.size());
            Card retrievedCard = cards.getFirst();
            assertEquals("******7890", retrievedCard.getCardNumber());
            assertEquals("***", retrievedCard.getCvv());
        }
    }

    @Test
    void getAccountCards_ShouldThrowException_WhenNoCardsFound() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                cardService.getAccountCards("bankName", bankAccount.getId(), request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No cards found for specified account.", exception.getMessage());
    }

    @Test
    void getAccountCardById_ShouldThrowException_WhenCardNotFound() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                cardService.getAccountCardById("bankName",
                        bankAccount.getId(),
                        UUID.randomUUID(),
                        request,
                        "visible"
                )
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Specified card not found.", exception.getMessage());
    }

    @Test
    void getAccountCardById_ShouldReturnCard_WhenCardExists() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(card.getCardNumber(), mockKey))
                    .thenReturn("1234567890");
            encryptionMock.when(() -> EncryptionUtil.decrypt(card.getCvv(), mockKey)).thenReturn("123");

            Card retrievedCard = cardService.getAccountCardById(
                    "bankName",
                    bankAccount.getId(),
                    card.getId(),
                    request,
                    "visible");

            assertNotNull(retrievedCard);
            assertEquals(card.getId(), retrievedCard.getId());
            assertEquals("1234567890", retrievedCard.getCardNumber());
            assertEquals("123", retrievedCard.getCvv());
        }
    }

    @Test
    void getAccountCardById_ShouldThrowException_WhenNoCardsFound() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                cardService.getAccountCardById(
                        "bankName",
                        bankAccount.getId(),
                        UUID.randomUUID(),
                        request,
                        "visible"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No cards found for specified account.", exception.getMessage());
    }

    @Test
    void addAccountCard_ShouldAddCard_WhenValidRequest() throws Exception {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card newCard = cardService.addAccountCard("bankName", bankAccount.getId(), request);

        assertNotNull(newCard);
        assertEquals(card.getId(), newCard.getId());
    }

    @Test
    void addAccountCard_ShouldThrowException_WhenBankAccountNotFound() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                cardService.addAccountCard("bankName", UUID.randomUUID(), request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No accounts found for specified bank identity.", exception.getMessage());
    }

    @Test
    void removeAccountCard_ShouldRemoveCard_WhenCardExists() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));

        cardService.removeAccountCard("bankName", bankAccount.getId(), card.getId(), request);

        verify(cardRepository).delete(card);
    }

    @Test
    void removeAccountCard_ShouldThrowException_WhenNoCardsFound() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(bankAccountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                cardService.removeAccountCard("bankName", bankAccount.getId(), UUID.randomUUID(), request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No cards found for specified account.", exception.getMessage());
    }

    @Test
    void removeAllCards_ShouldRemoveAllCards_WhenCardsExist() {
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));

        cardService.removeAllCards(bankAccount);

        verify(cardRepository).deleteAll(List.of(card));
    }

    @Test
    void removeAllCards_ShouldThrowException_WhenErrorOccurs() {
        when(cardRepository.findAllByAccountId(bankAccount.getId())).thenReturn(Optional.of(List.of(card)));
        doThrow(new RuntimeException("Database error")).when(cardRepository).deleteAll(anyList());

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                cardService.removeAllCards(bankAccount));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("Error while removing all cards.", exception.getMessage());
    }
}