package api.mpba.rastvdmy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.impl.AuthServiceImpl;
import api.mpba.rastvdmy.service.validator.CountryValidator;
import api.mpba.rastvdmy.service.generator.GenerateAccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private GenerateAccessToken generateAccessToken;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CountryValidator countryValidator;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserProfileRequest userProfileRequest;
    private UserLoginRequest userLoginRequest;
    private UserProfile userProfile;

    @BeforeEach
    public void setUp() {
        userProfileRequest = new UserProfileRequest(UUID.randomUUID(),
                "John",
                "Doe",
                "2001-01-01",
                "Czechia",
                "jhondoe@mpba.com",
                "Password123",
                "+420123456789",
                "User.png",
                UserStatus.STATUS_DEFAULT,
                UserRole.ROLE_DEFAULT);

        userLoginRequest = new UserLoginRequest("john.doe@example.com", "password123");
        userProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .name("John")
                .surname("Doe")
                .dateOfBirth("2001-01-01")
                .countryOrigin("Czechia")
                .email("jhondoe@mpba.com")
                .password("Password123")
                .phoneNumber("+420123456789")
                .avatar("User.png")
                .status(UserStatus.STATUS_DEFAULT)
                .role(UserRole.ROLE_DEFAULT)
                .build();

        // Inject the mocked RestTemplate into CountryValidator
        ReflectionTestUtils.setField(countryValidator, "restTemplate", restTemplate);
    }


    @Test
    public void signUp_ShouldThrowException_WhenEmailExists() {
        when(userProfileRepository.findByEmail(userProfileRequest.email())).thenReturn(Optional.of(userProfile));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> authService.signUp(userProfileRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("User with this email already exists.", exception.getMessage());
    }

    @Test
    public void authenticate_ShouldReturnJwtAuthResponse_WhenValidCredentials() {
        when(userProfileRepository.findByEmail(userLoginRequest.email())).thenReturn(Optional.of(userProfile));
        when(generateAccessToken.generate(any(UserProfile.class))).thenReturn(
                new GenerateAccessToken.TokenDetails("mockToken", 3600));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        JwtAuthResponse response = authService.authenticate(userLoginRequest);

        assertNotNull(response);
        assertEquals("mockToken", response.token());
        assertEquals(3600, response.expiresIn());
    }

    @Test
    public void authenticate_ShouldThrowException_WhenUserNotFound() {
        when(userProfileRepository.findByEmail(userLoginRequest.email())).thenReturn(Optional.empty());

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> authService.authenticate(userLoginRequest));

        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    public void authenticate_ShouldThrowException_WhenUserIsBlocked() {
        userProfile.setStatus(UserStatus.STATUS_BLOCKED);
        when(userProfileRepository.findByEmail(userLoginRequest.email())).thenReturn(Optional.of(userProfile));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> authService.authenticate(userLoginRequest));

        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        assertEquals("User is blocked. Authentication is forbidden.", exception.getMessage());
    }

    @Test
    public void authenticate_ShouldThrowException_WhenInvalidCredentials() {
        when(userProfileRepository.findByEmail(userLoginRequest.email())).thenReturn(Optional.of(userProfile));

        doThrow(new RuntimeException()).when(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class));

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> authService.authenticate(userLoginRequest));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
        assertEquals("Invalid email or password. Please try again.", exception.getMessage());
    }
}