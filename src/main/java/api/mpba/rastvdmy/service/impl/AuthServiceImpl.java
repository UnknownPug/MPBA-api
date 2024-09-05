package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.entity.AccessToken;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.AccessTokenRepository;
import api.mpba.rastvdmy.repository.CurrencyDataRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.AuthService;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.component.Generator;
import api.mpba.rastvdmy.service.component.UserDataValidator;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl extends Generator implements AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final api.mpba.rastvdmy.repository.AccessTokenRepository tokenRepository;
    private final CurrencyDataRepository currencyDataRepository;

    public AuthServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RestTemplate restTemplate,
            AccessTokenRepository tokenRepository,
            CurrencyDataRepository currencyDataRepository) {
        super(restTemplate);
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.currencyDataRepository = currencyDataRepository;
    }

    @Transactional
    public JwtAuthResponse signUp(UserRequest request) throws Exception {

        // Validate user data
        validateUserData(request);

        SecretKey secretKey = EncryptionUtil.generateKey();
        String encodedDateOfBirth = EncryptionUtil.encrypt(
                request.dateOfBirth(), secretKey, EncryptionUtil.generateIv()
        );

        List<CurrencyData> currencyData = currencyDataRepository.findAll();

        User user = User.builder()
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
                .phoneNumber(request.phoneNumber())
                .currencyData(currencyData)
                .build();
        userRepository.save(user);

        GenerateAccessToken generatedToken = getGenerateAccessToken(user);
        return JwtAuthResponse.builder()
                .token(generatedToken.token())
                .expiresIn(generatedToken.tokenExpiration())
                .build();
    }

    @Transactional
    public JwtAuthResponse authenticate(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );

        GenerateAccessToken generatedToken = getGenerateAccessToken(user);
        return JwtAuthResponse.builder()
                .token(generatedToken.token())
                .expiresIn(generatedToken.tokenExpiration())
                .build();
    }

    private GenerateAccessToken getGenerateAccessToken(User user) {
        String token = jwtService.generateToken(user);
        long tokenExpiration = jwtService.getExpirationTime();

        AccessToken accessToken = AccessToken.builder()
                .id(UUID.randomUUID())
                .token(token)
                .user(user)
                .expirationDate(LocalDateTime.now().plus(tokenExpiration, ChronoUnit.MILLIS))
                .build();

        tokenRepository.save(accessToken);
        return new GenerateAccessToken(token, tokenExpiration);
    }

    private record GenerateAccessToken(String token, long tokenExpiration) {
    }

    private void validateUserData(UserRequest request) {
        checkIfUserExistsByEmail(request);
        checkIfUserExistsByPhoneNumber(request);
        countryValidation(request);
        passwordValidation(request);
        phoneNumberValidation(request);
        dateOfBirthValidation(request);
    }

    private static void dateOfBirthValidation(UserRequest request) {
        if (!UserDataValidator.isValidDateOfBirth(request.dateOfBirth())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid date of birth. Provide the date of birth in the format ‘YYYY-MM-DD’ " +
                            "and ensure entered age between 18 and 100 years old.");
        }
    }

    private static void phoneNumberValidation(UserRequest request) {
        if (!UserDataValidator.isValidPhoneNumber(request.phoneNumber())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "An invalid phone number has been entered. The number must starts with ‘+’ " +
                            "and contain (1-3 digits) and main number (9-15 digits)");
        }
    }

    private static void passwordValidation(UserRequest request) {
        if (!UserDataValidator.isValidPassword(request.password())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Invalid password. Password must be between 8 and 20 characters long, contain at least one" +
                            " uppercase letter, and include at least one number or special character.");
        }
    }

    private void countryValidation(UserRequest request) {
        if (countryExists(request.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
    }

    private void checkIfUserExistsByPhoneNumber(UserRequest request) {
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with phone number " + request.phoneNumber() + " already exists."
            );
        }
    }

    private void checkIfUserExistsByEmail(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with email " + request.email() + " already exists."
            );
        }
    }
}