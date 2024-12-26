package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.entity.BankAccount;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.Card;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.*;
import api.mpba.rastvdmy.entity.enums.Currency;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankAccountRepository;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.CardRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.impl.BankAccountServiceImpl;
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
class BankAccountServiceImplTest {

    @Mock
    private BankAccountRepository accountRepository;

    @Mock
    private BankIdentityRepository bankIdentityRepository;

    @Mock
    private TokenVerifierService tokenVerifierService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @Mock
    private HttpServletRequest request;

    private UserProfile userProfile;
    private BankIdentity bankIdentity;
    private BankAccount bankAccount;

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

        Card card = Card.builder()
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

        when(tokenVerifierService.getUserData(request)).thenReturn(userProfile);
    }

    @Test
    void getUserAccounts_ShouldReturnAccounts_WhenAccountsExist() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(bankAccount.getIban(), mockKey))
                    .thenReturn("CZ1234567890");
            encryptionMock.when(() -> EncryptionUtil.decrypt(bankAccount.getAccountNumber(), mockKey))
                    .thenReturn("1234567890");

            List<BankAccount> accounts = bankAccountService.getUserAccounts(request, "bankName");

            assertNotNull(accounts);
            assertEquals(1, accounts.size());
            assertEquals(bankAccount, accounts.getFirst());
        }
    }

    @Test
    void getUserAccounts_ShouldThrowException_WhenNoAccountsFound() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.getUserAccounts(request, "bankName");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No accounts found.", exception.getMessage());
    }

    @Test
    void getAccountById_ShouldReturnAccount_WhenAccountExists() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::getSecretKey).thenReturn(mockKey);
            encryptionMock.when(() -> EncryptionUtil.decrypt(bankAccount.getIban(), mockKey))
                    .thenReturn("CZ1234567890");
            encryptionMock.when(() -> EncryptionUtil.decrypt(bankAccount.getAccountNumber(), mockKey))
                    .thenReturn("1234567890");

            BankAccount account = bankAccountService.getAccountById(
                    request,
                    "bankName",
                    bankAccount.getId(),
                    "visible"
            );

            assertNotNull(account);
            assertEquals(bankAccount, account);
        }
    }

    @Test
    void getAccountById_ShouldThrowException_WhenAccountNotFound() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.getAccountById(request, "bankName", bankAccount.getId(), any(String.class));
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Requested account not found.", exception.getMessage());
    }

    @Test
    void getTotalBalance_ShouldReturnTotalBalance_WhenAccountsExist() {
        when(bankIdentityRepository.findAllByUserProfileId(userProfile.getId()))
                .thenReturn(Optional.of(List.of(bankIdentity)));
        when(accountRepository.findAllByBankIdentitiesId(List.of(bankIdentity.getId())))
                .thenReturn(List.of(bankAccount));

        Map<String, BigDecimal> totalBalance = bankAccountService.getTotalBalance(request);

        assertNotNull(totalBalance);
        assertEquals(BigDecimal.valueOf(1000), totalBalance.get("CZK"));
    }

    @Test
    void getTotalBalance_ShouldThrowException_WhenNoBankIdentitiesFound() {
        when(bankIdentityRepository.findAllByUserProfileId(userProfile.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.getTotalBalance(request);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No bank identities found.", exception.getMessage());
    }

    @Test
    void addAccount_ShouldAddAccount_WhenValidRequest() throws Exception {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.save(any(BankAccount.class))).thenReturn(bankAccount);

        BankAccount newAccount = bankAccountService.addAccount(request, "CzechBank");

        assertNotNull(newAccount);
        assertEquals(bankAccount.getId(), newAccount.getId());
    }

    @Test
    void removeAccount_ShouldRemoveAccount_WhenAccountExists() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));

        bankAccountService.removeAccount(request, "CzechBank", bankAccount.getId());

        verify(accountRepository).delete(bankAccount);
    }

    @Test
    void removeAccount_ShouldThrowException_WhenAccountsNotFound() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.removeAccount(request, "CzechBank", bankAccount.getId());
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Requested account not found.", exception.getMessage());
    }

    @Test
    void removeAccount_ShouldThrowException_WhenSpecificAccountNotFound() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));

        UUID nonExistentAccountId = UUID.randomUUID();

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.removeAccount(request, "CzechBank", nonExistentAccountId);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Requested account not found.", exception.getMessage());
    }

    @Test
    void removeAllAccounts_ShouldRemoveAllAccounts_WhenAccountsExist() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(List.of(bankAccount)));

        bankAccountService.removeAllAccounts(request, "CzechBank");

        verify(accountRepository).deleteAll(List.of(bankAccount));
    }

    @Test
    void removeAllAccounts_ShouldThrowException_WhenBankIdentityNotFound() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.removeAllAccounts(request, "CzechBank");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Bank identity not found.", exception.getMessage());
    }

    @Test
    void removeAllAccounts_ShouldThrowException_WhenAccountsAreEmpty() {
        when(bankIdentityRepository.findByUserProfileIdAndBankName(userProfile.getId(), "CzechBank"))
                .thenReturn(Optional.of(bankIdentity));
        when(accountRepository.findAllByBankIdentityId(bankIdentity.getId()))
                .thenReturn(Optional.of(Collections.emptyList()));

        ApplicationException exception = assertThrows(ApplicationException.class, () -> {
            bankAccountService.removeAllAccounts(request, "CzechBank");
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("No accounts are found for the specified bank identity.", exception.getMessage());
    }
}