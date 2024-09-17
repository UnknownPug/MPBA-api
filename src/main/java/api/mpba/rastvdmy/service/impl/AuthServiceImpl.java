package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.UserLoginRequest;
import api.mpba.rastvdmy.dto.request.UserRequest;
import api.mpba.rastvdmy.dto.response.JwtAuthResponse;
import api.mpba.rastvdmy.entity.CurrencyData;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.CurrencyDataRepository;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.AuthService;
import api.mpba.rastvdmy.service.utils.CountryValidator;
import api.mpba.rastvdmy.service.utils.FinancialDataGenerator;
import api.mpba.rastvdmy.service.utils.GenerateAccessToken;
import api.mpba.rastvdmy.service.utils.UserDataValidator;
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

@Service
public class AuthServiceImpl extends FinancialDataGenerator implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CurrencyDataRepository currencyDataRepository;
    private final RestTemplate restTemplate;
    private final GenerateAccessToken generateAccessToken;

    public AuthServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            CurrencyDataRepository currencyDataRepository,
            RestTemplate restTemplate, GenerateAccessToken generateAccessToken) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyDataRepository = currencyDataRepository;
        this.restTemplate = restTemplate;
        this.generateAccessToken = generateAccessToken;
    }

    @Transactional
    public JwtAuthResponse signUp(UserRequest request) throws Exception {

        // Validate user data
        UserDataValidator.isInvalidPassword(request.password());
        UserDataValidator.isInvalidPhoneNumber(request.phoneNumber());
        UserDataValidator.isInvalidDateOfBirth(request.dateOfBirth());
        validateUserData(request);

        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encodedDateOfBirth = EncryptionUtil.encrypt(request.dateOfBirth(), secretKey);

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

        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(user);

        return JwtAuthResponse.builder()
                .token(generatedToken.token())
                .expiresIn(generatedToken.tokenExpiration())
                .build();
    }

    @Transactional
    public JwtAuthResponse authenticate(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );

        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "User is blocked. Authentication is forbidden.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Invalid email or password. Please try again.");
        }
        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(user);

        return JwtAuthResponse.builder()
                .token(generatedToken.token())
                .expiresIn(generatedToken.tokenExpiration())
                .build();
    }

    private void validateUserData(UserRequest request) {
        checkIfUserExistsByEmail(request);
        checkIfUserExistsByPhoneNumber(request);
        countryValidation(request);
    }

    private void checkIfUserExistsByEmail(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with email " + request.email() + " already exists."
            );
        }
    }

    private void checkIfUserExistsByPhoneNumber(UserRequest request) {
        if (userRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with phone number " + request.phoneNumber() + " already exists."
            );
        }
    }

    private void countryValidation(UserRequest request) {
        CountryValidator countryValidator = new CountryValidator(restTemplate);
        if (countryValidator.countryExists(request.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
    }
}