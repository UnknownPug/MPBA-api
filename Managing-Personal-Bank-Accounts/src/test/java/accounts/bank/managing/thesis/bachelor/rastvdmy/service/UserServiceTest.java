package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.UserStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.UserVisibility;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class is used to test the functionality of the UserService class.
 * It uses the Mockito framework for mocking dependencies and JUnit for running the tests.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrencyDataRepository currencyDataRepository;
    @Mock
    private CardService cardService;
    @Mock
    private UserService userService;

    /**
     * This method is used to set up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        currencyDataRepository = mock(CurrencyDataRepository.class);
        cardService = mock(CardService.class);
        userService = new UserService(userRepository, passwordEncoder, currencyDataRepository, cardService);
    }

    /**
     * This method tests the functionality of the getUsers method in the UserService class.
     * It verifies that the method returns all users in the repository.
     */
    @Test
    void testGetUsers() {
        // Mocking data
        List<User> users = new ArrayList<>();
        users.add(new User());
        when(userRepository.findAll()).thenReturn(users);

        // Testing the method
        List<User> result = userService.getUsers();

        // Assertions
        assert result.size() == 1; // Ensure one user is returned
    }

    /**
     * This method tests the functionality of the filterAndSortUsers method in the UserService class.
     * It verifies that the method returns a page of users sorted and filtered
     * according to the provided Pageable object.
     */
    @Test
    void testFilterAndSortUsers() {
        // Mocking data
        Pageable pageable = Pageable.unpaged();
        List<User> users = new ArrayList<>();
        users.add(new User());
        Page<User> page = new PageImpl<>(users, pageable, users.size());
        when(userRepository.findAll(pageable)).thenReturn(page);

        // Testing the method
        Page<User> result = userService.filterAndSortUsers(pageable);

        // Assertions
        assert result.getTotalElements() == 1; // Ensure one user is returned
    }

    /**
     * This method tests the functionality of the getUserById method in the UserService class.
     * It verifies that the method returns the correct user when a valid ID is provided.
     */
    @Test
    void testGetUserById_ExistingId() {
        // Mocking data
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Testing the method
        User result = userService.getUserById(userId);

        // Assertions
        assert result.getId().equals(userId); // Ensure the correct user is returned
    }

    /**
     * This method tests the functionality of the getUserById method in the UserService class.
     * It verifies that the method throws an exception when an invalid ID is provided.
     */
    @Test
    void testGetUserById_NonExistingId() {
        // Mocking data
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Testing the method and expecting an exception
        try {
            userService.getUserById(userId);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when all fields are empty.
     */
    @Test
    public void testCreateUser_AllFieldsEmpty() {
        assertThrows(ApplicationException.class, () -> userService.createUser("", "", null, "", "", "", ""));
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when the email already exists.
     */
    @Test
    public void testCreateUser_EmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(ApplicationException.class, () -> userService.createUser("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "john.doe@example.com", "password123", "+1234567890"));
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when the phone number already exists.
     */
    @Test
    public void testCreateUser_PhoneNumberExists() {
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);
        assertThrows(ApplicationException.class, () -> userService.createUser("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "john.doe@example.com", "password123", "+1234567890"));
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when the name is invalid.
     */
    @Test
    public void testCreateUser_InvalidName() {
        assertThrows(ApplicationException.class, () -> userService.createUser("J", "Doe", LocalDate.of(1990, 1, 1), "USA", "john.doe@example.com", "password123", "+1234567890"));
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when the surname is invalid.
     */
    @Test
    public void testCreateUser_InvalidSurname() {
        assertThrows(ApplicationException.class, () -> userService.createUser("John", "D", LocalDate.of(1990, 1, 1), "USA", "john.doe@example.com", "password123", "+1234567890"));
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when the date of birth is invalid.
     */
    @Test
    public void testCreateUser_InvalidDateOfBirth() {
        assertThrows(ApplicationException.class, () -> userService.createUser("John", "Doe", LocalDate.of(2022, 1, 1), "USA", "john.doe@example.com", "password123", "+1234567890"));
    }

    /**
     * This method tests the functionality of the createUser method in the UserService class.
     * It verifies that the method throws an exception when the country does not exist.
     */
    @Test
    public void testCreateUser_CountryDoesNotExist() {
        assertThrows(ApplicationException.class, () -> userService.createUser("John", "Doe", LocalDate.of(1990, 1, 1), "USA", "john.doe@example.com", "password123", "+1234567890"));
    }

    /**
     * This method tests the functionality of the updateUserById method in the UserService class.
     * It verifies that the method updates the user when valid user ID, email, password, and phone number are provided.
     */
    @Test
    void testUpdateUserById_ValidInput() {
        // Mocking data
        Long userId = 1L;
        String email = "test@example.com";
        String password = "TestPassword123!";
        String phoneNumber = "+1234567890";
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // Testing the method
        userService.updateUserById(userId, email, password, phoneNumber);

        // Verify that userRepository.save was called once with the updated user
        verify(userRepository).save(any(User.class));
    }

    /**
     * This method tests the functionality of the updateUserById method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    void testUpdateUserById_UserNotFound() {
        // Mocking data
        Long userId = 1L;
        String email = "test@example.com";
        String password = "TestPassword123!";
        String phoneNumber = "+1234567890";
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Testing the method and expecting an exception
        try {
            userService.updateUserById(userId, email, password, phoneNumber);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the uploadUserAvatar method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    public void testUploadUserAvatar_UserNotFound() {
        Long userId = 1L;
        MultipartFile userAvatar = mock(MultipartFile.class);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> userService.uploadUserAvatar(userId, userAvatar));
    }

    /**
     * This method tests the functionality of the uploadUserAvatar method in the UserService class.
     * It verifies that the method throws an exception when the user is blocked.
     */
    @Test
    public void testUploadUserAvatar_UserBlocked() {
        Long userId = 1L;
        MultipartFile userAvatar = mock(MultipartFile.class);

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_BLOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () -> userService.uploadUserAvatar(userId, userAvatar));
    }

    /**
     * This method tests the functionality of the uploadUserAvatar method in the UserService class.
     * It verifies that the method throws an exception when the uploaded file is not an image.
     */
    @Test
    public void testUploadUserAvatar_NotAnImage() {
        Long userId = 1L;
        MultipartFile userAvatar = mock(MultipartFile.class);

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAvatar.getContentType()).thenReturn("text/plain");

        assertThrows(ApplicationException.class, () -> userService.uploadUserAvatar(userId, userAvatar));
    }

    /**
     * This method tests the functionality of the uploadUserAvatar method in the UserService class.
     * It verifies that the method uploads the avatar when valid user ID and image file are provided.
     */
    @Test
    public void testUploadUserAvatar_ValidInputs() {
        Long userId = 1L;
        MultipartFile userAvatar = mock(MultipartFile.class);

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userAvatar.getContentType()).thenReturn("image/png");
        when(userAvatar.getOriginalFilename()).thenReturn("avatar.png");

        userService.uploadUserAvatar(userId, userAvatar);

        verify(user, times(1)).setAvatar("avatar.png");
        verify(userRepository, times(1)).save(user);
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    public void testUpdateUserEmailById_UserNotFound() {
        Long userId = 1L;
        String email = "john.doe@example.com";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> userService.updateUserEmailById(userId, email));
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method throws an exception when the user is blocked.
     */
    @Test
    public void testUpdateUserEmailById_UserBlocked() {
        Long userId = 1L;
        String email = "john.doe@example.com";

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_BLOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () -> userService.updateUserEmailById(userId, email));
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method throws an exception when the email is null.
     */
    @Test
    public void testUpdateUserEmailById_EmailNull() {
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () -> userService.updateUserEmailById(userId, null));
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method throws an exception when the new email is the same as the old one.
     */
    @Test
    public void testUpdateUserEmailById_EmailSameAsOld() {
        Long userId = 1L;
        String email = "john.doe@example.com";

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(user.getEmail()).thenReturn(email);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () -> userService.updateUserEmailById(userId, email));
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method throws an exception when the email already exists.
     */
    @Test
    public void testUpdateUserEmailById_EmailExists() {
        Long userId = 1L;
        String email = "john.doe@example.com";

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(ApplicationException.class, () -> userService.updateUserEmailById(userId, email));
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method throws an exception when the email is invalid.
     */
    @Test
    public void testUpdateUserEmailById_InvalidEmail() {
        Long userId = 1L;
        String email = "invalid email";

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () -> userService.updateUserEmailById(userId, email));
    }

    /**
     * This method tests the functionality of the updateUserEmailById method in the UserService class.
     * It verifies that the method updates the email when valid user ID and email are provided.
     */
    @Test
    public void testUpdateUserEmailById_ValidInputs() {
        Long userId = 1L;
        String email = "john.doe@example.com";

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(email)).thenReturn(false);

        userService.updateUserEmailById(userId, email);

        verify(user, times(1)).setEmail("john.doe@example.com");
        verify(userRepository, times(1)).save(user);
    }

    /**
     * This method tests the functionality of the updateUserPasswordById method in the UserService class.
     * It verifies that the method updates the password when valid user ID and password are provided.
     */
    @Test
    void testUpdateUserPasswordById_ValidInput() {
        // Mocking data
        Long userId = 1L;
        String password = "TestPassword123!";
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // Testing the method
        userService.updateUserPasswordById(userId, password);

        // Verify that userRepository.save was called once with the updated user
        verify(userRepository).save(any(User.class));
    }

    /**
     * This method tests the functionality of the updateUserPasswordById method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    void testUpdateUserPasswordById_UserNotFound() {
        // Mocking data
        Long userId = 1L;
        String password = "TestPassword123!";
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Testing the method and expecting an exception
        try {
            userService.updateUserPasswordById(userId, password);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }

    /**
     * This method tests the functionality of the updateUserStatusById method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    public void testUpdateUserStatusById_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> userService.updateUserStatusById(userId));
    }

    /**
     * This method tests the functionality of the updateUserStatusById method in the UserService class.
     * It verifies that the method updates the user status when the user is unblocked.
     */
    @Test
    public void testUpdateUserStatusById_UserUnblocked() {
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_UNBLOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserStatusById(userId);

        verify(user, times(1)).setStatus(UserStatus.STATUS_BLOCKED);
        verify(userRepository, times(1)).save(user);
    }

    /**
     * This method tests the functionality of the updateUserStatusById method in the UserService class.
     * It verifies that the method updates the user status when the user is blocked.
     */
    @Test
    public void testUpdateUserStatusById_UserBlocked() {
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_BLOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserStatusById(userId);

        verify(user, times(1)).setStatus(UserStatus.STATUS_UNBLOCKED);
        verify(userRepository, times(1)).save(user);
    }

    /**
     * This method tests the functionality of the updateUserVisibilityById method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    public void testUpdateUserVisibilityById_UserNotFound() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> userService.updateUserVisibilityById(userId));
    }

    /**
     * This method tests the functionality of the updateUserVisibilityById method in the UserService class.
     * It verifies that the method throws an exception when the user is blocked.
     */
    @Test
    public void testUpdateUserVisibilityById_UserBlocked() {
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_BLOCKED);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ApplicationException.class, () -> userService.updateUserVisibilityById(userId));
    }

    /**
     * This method tests the functionality of the updateUserVisibilityById method in the UserService class.
     * It verifies that the method updates the user visibility when the user is online.
     */
    @Test
    public void testUpdateUserVisibilityById_UserOnline() {
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_UNBLOCKED);
        when(user.getVisibility()).thenReturn(UserVisibility.STATUS_ONLINE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserVisibilityById(userId);

        verify(user, times(1)).setVisibility(UserVisibility.STATUS_OFFLINE);
        verify(userRepository, times(1)).save(user);
    }

    /**
     * This method tests the functionality of the updateUserVisibilityById method in the UserService class.
     * It verifies that the method updates the user visibility when the user is offline.
     */
    @Test
    public void testUpdateUserVisibilityById_UserOffline() {
        Long userId = 1L;

        User user = mock(User.class);
        when(user.getStatus()).thenReturn(UserStatus.STATUS_UNBLOCKED);
        when(user.getVisibility()).thenReturn(UserVisibility.STATUS_OFFLINE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.updateUserVisibilityById(userId);

        verify(user, times(1)).setVisibility(UserVisibility.STATUS_ONLINE);
        verify(userRepository, times(1)).save(user);
    }

    /**
     * This method tests the functionality of the updateUserPhoneNumberById method in the UserService class.
     * It verifies that the method updates the phone number when valid user ID and phone number are provided.
     */
    @Test
    void testUpdateUserPhoneNumberById_ValidInput() {
        // Mocking data
        Long userId = 1L;
        String phoneNumber = "+1234567890";
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.STATUS_DEFAULT);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);

        // Testing the method
        userService.updateUserPhoneNumberById(userId, phoneNumber);

        // Verify that userRepository.save was called once with the updated user
        verify(userRepository).save(any(User.class));
    }

    /**
     * This method tests the functionality of the updateUserPhoneNumberById method in the UserService class.
     * It verifies that the method throws an exception when the user is not found.
     */
    @Test
    void testUpdateUserPhoneNumberById_UserNotFound() {
        // Mocking data
        Long userId = 1L;
        String phoneNumber = "+1234567890";
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // Testing the method and expecting an exception
        try {
            userService.updateUserPhoneNumberById(userId, phoneNumber);
        } catch (ApplicationException e) {
            // Assertions
            assert e.getHttpStatus().equals(HttpStatus.NOT_FOUND); // Ensure correct exception is thrown
        }
    }
}

