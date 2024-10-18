package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.entity.BankIdentity;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.BankIdentityRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MockHttpServletRequest request;

    @Mock
    private BankIdentityRepository bankIdentityRepository;

    @Mock
    private AdminUpdateUserRequest userRequest;

    @InjectMocks
    private UserProfileServiceImpl userService;

    private UserProfile userProfile;

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

        BankIdentity bankIdentity = BankIdentity.builder()
                .id(UUID.randomUUID())
                .bankName("CzechBank")
                .bankNumber("123456")
                .swift("CZBACZPP")
                .userProfile(userProfile)
                .bankAccounts(null)
                .build();

        userProfile.setBankIdentities(List.of(bankIdentity));

        bankIdentityRepository.save(bankIdentity);
    }

    @Test
    void getUsers_ShouldReturnListOfUsers() {
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(userProfileRepository.findAll()).thenReturn(List.of(userProfile));
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::generateKey).thenReturn(mockKey);
            encryptionMock.when(
                    () -> EncryptionUtil.decrypt(userProfile.getDateOfBirth(), mockKey)
            ).thenReturn("2001-01-01");
            encryptionMock.when(() -> EncryptionUtil.decrypt(userProfile.getPhoneNumber(), mockKey)
            ).thenReturn("+420123456789");

            List<UserProfile> userProfiles = userService.getUsers(request);

            assertNotNull(userProfiles);
            assertEquals(1, userProfiles.size());
            verify(userProfileRepository).findAll();
        }
    }

    @Test
    void filterAndSortUsers_ShouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserProfile> userPage = new PageImpl<>(List.of(userProfile));
        when(userProfileRepository.findAll(pageable)).thenReturn(userPage);
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::generateKey).thenReturn(mockKey);
            encryptionMock.when(
                    () -> EncryptionUtil.decrypt(userProfile.getDateOfBirth(), mockKey)
            ).thenReturn("2001-01-01");
            encryptionMock.when(
                    () -> EncryptionUtil.decrypt(userProfile.getPhoneNumber(), mockKey)
            ).thenReturn("+420123456789");


            Page<UserProfile> result = userService.filterAndSortUsers(request, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(userProfileRepository).findAll(pageable);
        }
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(userProfileRepository.findById(userProfile.getId())).thenReturn(Optional.of(userProfile));
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        try (MockedStatic<EncryptionUtil> encryptionMock = Mockito.mockStatic(EncryptionUtil.class)) {
            SecretKey mockKey = mock(SecretKey.class);

            encryptionMock.when(EncryptionUtil::generateKey).thenReturn(mockKey);
            encryptionMock.when(
                    () -> EncryptionUtil.decrypt(userProfile.getDateOfBirth(), mockKey)
            ).thenReturn("2001-01-01");
            encryptionMock.when(
                    () -> EncryptionUtil.decrypt(userProfile.getPhoneNumber(), mockKey)
            ).thenReturn("+420123456789");

            UserProfile result = userService.getUserById(request, userProfile.getId());

            assertNotNull(result);
            assertEquals(userProfile.getId(), result.getId());
            verify(userProfileRepository).findById(userProfile.getId());
        }
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() throws Exception {
        UserUpdateRequest userRequest = new UserUpdateRequest(
                "new@example.com",
                "Qwertyuiop123",
                "+420987654321"
        );
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(passwordEncoder.encode(userRequest.password())).thenReturn("encodedPassword");
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        UserProfile result = userService.updateUser(request, userRequest);

        assertNotNull(result);
        assertEquals(userRequest.email(), result.getEmail());
        verify(userProfileRepository).save(any(UserProfile.class));
    }


    @Test
    void uploadUserAvatar_ShouldUploadAvatar() {
        MultipartFile userAvatar = mock(MultipartFile.class);
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(userAvatar.getContentType()).thenReturn("image/png");
        when(userAvatar.getOriginalFilename()).thenReturn("avatar.png");
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        userService.uploadUserAvatar(request, userAvatar);

        assertEquals("avatar.png", userProfile.getAvatar());
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void removeUserAvatar_ShouldRemoveAvatar() {
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        userService.removeUserAvatar(request);

        assertEquals(
                "https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1",
                userProfile.getAvatar()
        );
        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void updateUserRole_ShouldUpdateRole() {
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("anotherUserId");
        when(userProfileRepository.findById(userProfile.getId())).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        userService.updateUserRole(request, userProfile.getId());

        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void updateUserStatus_ShouldUpdateStatus() {
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(jwtService.extractToken(request)).thenReturn("token");
        when(jwtService.extractSubject("token")).thenReturn("anotherUserId");
        when(userProfileRepository.findById(userProfile.getId())).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.of(userProfile));

        userService.updateUserStatus(request, userProfile.getId());

        verify(userProfileRepository).save(userProfile);
    }

    @Test
    void testUpdateUserSpecificCredentials_InvalidSurname() {
        // Arrange
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());

        // Invalid surname
        AdminUpdateUserRequest userRequest = new AdminUpdateUserRequest("", "Czechia");

        // Act & Assert
        assertThrows(
                ApplicationException.class,
                () -> userService.updateUserSpecificCredentials(request, userProfile.getId(), userRequest)
        );
    }

    @Test
    void testUpdateUserSpecificCredentials_InvalidCountry() {
        // Arrange
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());

        // Initialize userRequest
        AdminUpdateUserRequest userRequest = new AdminUpdateUserRequest(
                "Surname",
                "InvalidCountry"
        );

        // Act & Assert
        assertThrows(
                ApplicationException.class,
                () -> userService.updateUserSpecificCredentials(request, userProfile.getId(), userRequest)
        );
    }

    @Test
    void testUpdateUserSpecificCredentials_UserNotFound() {
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        when(userProfileRepository.findByEmail(userProfile.getEmail())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ApplicationException.class,
                () -> userService.updateUserSpecificCredentials(request, userProfile.getId(), userRequest)
        );

        assertEquals("User does not exist.", exception.getMessage());
    }

    @Test
    public void deleteUser_shouldDeleteUser_whenNoBankIdentities() {
        userProfile.setBankIdentities(Collections.emptyList());
        when(request.getUserPrincipal()).thenReturn(() -> "test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.ofNullable(userProfile));

        userService.deleteUser(request);

        verify(userProfileRepository, times(1)).delete(userProfile);
    }

    @Test
    public void deleteUser_shouldThrowApplicationException_whenUserHasBankIdentities() {
        // Setting up user with bank identities
        userProfile.setBankIdentities(Collections.singletonList(new BankIdentity()));

        when(request.getUserPrincipal()).thenReturn(() -> "test@example.com");
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.ofNullable(userProfile));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> userService.deleteUser(request)
        );

        assertEquals("Make sure to delete all bank accounts first.", exception.getMessage());

        verify(userProfileRepository, never()).delete(userProfile);
    }

    @Test
    public void deleteUser_shouldThrowApplicationException_whenUserNotExist() {
        when(request.getUserPrincipal()).thenReturn(() -> "nonexistent@example.com");
        when(userProfileRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> userService.deleteUser(request)
        );

        assertEquals("User does not exist.", exception.getMessage());
        verify(userProfileRepository, never()).delete(any());
    }

    @Test
    void deleteUserByEmail_ShouldDeleteUser() {
        when(request.getUserPrincipal()).thenReturn(() -> userProfile.getEmail());
        assertThrows(ApplicationException.class, () -> userService.deleteUserByEmail(request, userProfile.getId()));
        verify(userProfileRepository, never()).delete(userProfile);
    }
}