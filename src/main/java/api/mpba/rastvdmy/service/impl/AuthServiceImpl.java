package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserProfileRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.CurrencyDataRepository;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.AuthService;
import api.mpba.rastvdmy.service.validator.CountryValidator;
import api.mpba.rastvdmy.service.generator.FinancialDataGenerator;
import api.mpba.rastvdmy.service.generator.GenerateAccessToken;
import api.mpba.rastvdmy.service.validator.UserDataValidator;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

/**
 * This class implements the AuthService interface and provides functionalities for user authentication
 * and registration within the banking application.
 * It manages user profiles, authentication tokens,
 * and ensures data validation during the signup and login processes.
 */
@Service
public class AuthServiceImpl extends FinancialDataGenerator implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository userProfileRepository;
    private final CurrencyDataRepository currencyDataRepository;
    private final RestTemplate restTemplate;
    private final GenerateAccessToken generateAccessToken;

    /**
     * Constructor for the AuthServiceImpl class.
     *
     * @param userProfileRepository  Repository for user profile operations.
     * @param authenticationManager  Manages the authentication processes.
     * @param passwordEncoder        Encodes user passwords for secure storage.
     * @param currencyDataRepository Repository for currency data operations.
     * @param restTemplate           Used for REST API calls.
     * @param generateAccessToken    Generates access tokens for authenticated users.
     */
    public AuthServiceImpl(
            UserProfileRepository userProfileRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            CurrencyDataRepository currencyDataRepository,
            RestTemplate restTemplate, GenerateAccessToken generateAccessToken) {
        this.authenticationManager = authenticationManager;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyDataRepository = currencyDataRepository;
        this.restTemplate = restTemplate;
        this.generateAccessToken = generateAccessToken;
    }

    /**
     * Registers a new user profile in the system.
     *
     * @param request Contains the user profile details to register.
     * @return JwtAuthResponse containing the generated JWT token and its expiration details.
     * @throws Exception if there is an error during the registration process.
     */
    @Transactional
    public JwtAuthResponse signUp(UserProfileRequest request) throws Exception {

        // Validate user data
        UserDataValidator.isInvalidName(request.name());
        UserDataValidator.isInvalidSurname(request.surname());
        UserDataValidator.isInvalidEmail(request.email());
        UserDataValidator.isInvalidPassword(request.password());
        UserDataValidator.isInvalidPhoneNumber(request.phoneNumber());
        UserDataValidator.isInvalidDateOfBirth(request.dateOfBirth());
        validateUserData(request);

        // Encrypt sensitive data before saving
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encodedDateOfBirth = EncryptionUtil.encrypt(request.dateOfBirth(), secretKey);
        String encodedPhoneNumber = EncryptionUtil.encrypt(request.phoneNumber(), secretKey);

        // Retrieve all available currency data
        List<CurrencyData> currencyData = currencyDataRepository.findAll();

        // Create and save the new user profile
        UserProfile userProfile = UserProfile.builder()
                .id(UUID.randomUUID())
                .role(UserRole.ROLE_DEFAULT)
                .status(UserStatus.STATUS_DEFAULT)
                .name(request.name())
                .surname(request.surname())
                .dateOfBirth(encodedDateOfBirth)
                .countryOrigin(request.countryOfOrigin())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .avatar("https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1")
                .phoneNumber(encodedPhoneNumber)
                .currencyData(currencyData)
                .build();
        userProfileRepository.save(userProfile);

        // Generate an access token for the newly registered user
        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(userProfile);

        return JwtAuthResponse.builder()
                .token(generatedToken.token())
                .expiresIn(generatedToken.tokenExpiration())
                .build();
    }

    /**
     * Authenticates an existing user and generates an access token.
     *
     * @param request Contains the login credentials (email and password).
     * @return JwtAuthResponse containing the generated JWT token and its expiration details.
     */
    @Transactional
    public JwtAuthResponse authenticate(UserLoginRequest request) {

        // Retrieve user profile by email
        UserProfile userProfile = userProfileRepository.findByEmail(request.email()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );

        // Check if the user's account is blocked
        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "User is blocked. Authentication is forbidden.");
        }

        try {
            // Authenticate the user with provided credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password. Please try again.");
        }

        // Generate access token upon successful authentication
        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(userProfile);

        return JwtAuthResponse.builder()
                .token(generatedToken.token())
                .expiresIn(generatedToken.tokenExpiration())
                .build();
    }

    /**
     * Validates user data during signup.
     *
     * @param request Contains the user profile details to validate.
     */
    private void validateUserData(UserProfileRequest request) {
        checkIfUserExistsByEmail(request);
        checkIfUserExistsByPhoneNumber(request);
        countryValidation(request);
    }

    /**
     * Checks if a user already exists with the provided email address.
     *
     * @param request Contains the user profile details.
     */
    private void checkIfUserExistsByEmail(UserProfileRequest request) {
        if (userProfileRepository.findByEmail(request.email()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with this email already exists."
            );
        }
    }

    /**
     * Checks if a user already exists with the provided phone number.
     *
     * @param request Contains the user profile details.
     */
    private void checkIfUserExistsByPhoneNumber(UserProfileRequest request) {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        boolean phoneNumberExists = userProfiles.stream()
                .anyMatch(u -> isPhoneNumberExists(u, request));
        if (phoneNumberExists) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with this phone number already exists."
            );
        }
    }

    /**
     * Checks if the phone number exists among the user profiles.
     *
     * @param userProfile The user profile to check against.
     * @param request     Contains the user profile details for comparison.
     * @return True if the phone number exists, false otherwise.
     */
    private boolean isPhoneNumberExists(UserProfile userProfile, UserProfileRequest request) {
        SecretKey secretKey = EncryptionUtil.getSecretKey();
        try {
            String decryptedPhoneNumber = EncryptionUtil.decrypt(userProfile.getPhoneNumber(), secretKey);
            return decryptedPhoneNumber.equals(request.phoneNumber());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates the country of origin provided during signup.
     *
     * @param request Contains the user profile details to validate.
     */
    private void countryValidation(UserProfileRequest request) {
        CountryValidator countryValidator = new CountryValidator(restTemplate);
        if (countryValidator.countryExists(request.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
    }
}