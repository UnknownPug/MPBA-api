package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.impl.BankIdentityServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class BankIdentityServiceImplTest {

    @Mock
    private BankIdentityRepository identityRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private BankAccountService accountService;

    @InjectMocks
    private BankIdentityServiceImpl bankIdentityService;

    @Mock
    private HttpServletRequest request;

    private UserProfile userProfile;
    private BankIdentity bankIdentity;

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
        identityRepository.save(bankIdentity);
    }

    @Test
    void getBanks_ShouldReturnBanks_WhenBanksExist() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(identityRepository.findAllByUserProfileId(userProfile.getId()))
                .thenReturn(Optional.of(List.of(bankIdentity)));

        List<BankIdentity> banks = bankIdentityService.getBanks(request);

        assertNotNull(banks);
        assertEquals(1, banks.size());
        assertEquals(bankIdentity, banks.getFirst());
    }

    @Test
    void getBanks_ShouldThrowException_WhenUserBlocked() {
        userProfile.setStatus(UserStatus.STATUS_BLOCKED);
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                bankIdentityService.getBanks(request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("Operation is forbidden. User is blocked.", exception.getMessage());
    }

    @Test
    void getBankByName_ShouldReturnBank_WhenBankExists() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(identityRepository.findByNameAndConnectedToUserId("bankName", userProfile.getId()))
                .thenReturn(Optional.of(bankIdentity));

        BankIdentity bank = bankIdentityService.getBankByName(request, "bankName");

        assertNotNull(bank);
        assertEquals(bankIdentity, bank);
    }

    @Test
    void getBankByName_ShouldThrowException_WhenUserBlocked() {
        userProfile.setStatus(UserStatus.STATUS_BLOCKED);
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                bankIdentityService.getBankByName(request, "bankName"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("Operation is forbidden. User is blocked.", exception.getMessage());
    }

    @Test
    void addBank_ShouldAddBank_WhenValidRequest() throws Exception {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(identityRepository.save(any(BankIdentity.class))).thenReturn(bankIdentity);

        BankIdentity newBank = bankIdentityService.addBank(request);

        assertNotNull(newBank);
        assertEquals(bankIdentity.getId(), newBank.getId());
    }

    @Test
    void deleteBank_ShouldDeleteBank_WhenBankExists() {
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(identityRepository.findByUserIdAndBankNameWithAccounts(userProfile.getId(), "bankName"))
                .thenReturn(Optional.of(bankIdentity));

        bankIdentityService.deleteBank(request, "bankName");

        verify(identityRepository).delete(bankIdentity);
    }

    @Test
    void deleteBank_ShouldThrowException_WhenUserBlocked() {
        userProfile.setStatus(UserStatus.STATUS_BLOCKED);
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn(userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        ApplicationException exception = assertThrows(ApplicationException.class, () ->
                bankIdentityService.deleteBank(request, "bankName"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("Operation is forbidden. User is blocked.", exception.getMessage());
    }
}