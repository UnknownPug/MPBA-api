package accounts.bank.managing.thesis.bachelor.rastvdmy.service.impl;

import accounts.bank.managing.thesis.bachelor.rastvdmy.config.utils.EncryptionUtil;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserLoginRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.request.UserRequest;
import accounts.bank.managing.thesis.bachelor.rastvdmy.dto.response.JwtAuthResponse;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.AccessToken;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.User;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.UserRole;
import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.enums.UserStatus;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.AccessTokenRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.AuthService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.JwtService;
import accounts.bank.managing.thesis.bachelor.rastvdmy.service.component.Generator;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.List;

@Service
public class AuthServiceImpl extends Generator implements AuthService {
    private final UserRepository userRepository;
    private final AccessTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RestTemplate restTemplate,
            AccessTokenRepository tokenRepository
    ) {
        super(restTemplate);
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public JwtAuthResponse signUp(UserRequest input) throws Exception {

        if (countryExists(input.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist");
        }

        SecretKey secretKey = EncryptionUtil.generateKey();

        String encodedEmail = EncryptionUtil.encrypt(input.email(), secretKey, EncryptionUtil.generateIv());
        LocalDate encodedDateOfBirth = LocalDate.parse(EncryptionUtil.decrypt(
                input.dateOfBirth().toString(), secretKey, EncryptionUtil.generateIv()
        ));
        String encodedPhoneNumber = EncryptionUtil.encrypt(input.phoneNumber(), secretKey, EncryptionUtil.generateIv());

        User user = User.builder()
                .role(UserRole.ROLE_DEFAULT)
                .status(UserStatus.STATUS_DEFAULT)
                .name(input.name())
                .surname(input.surname())
                .dateOfBirth(encodedDateOfBirth)
                .countryOrigin(input.countryOfOrigin())
                .email(encodedEmail)
                .password(passwordEncoder.encode(input.password()))
                .phoneNumber(encodedPhoneNumber)
                .avatar("https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1")
                .build();

        String token = jwtService.generateToken((UserDetails) user);
        AccessToken accessToken = AccessToken.builder()
                .token(token)
                .user(user)
                .build();

        user.setAccessTokens(List.of(accessToken));

        userRepository.save(user);
        tokenRepository.save(accessToken);

        return JwtAuthResponse.builder().token(token).build();
    }

    @Override
    public JwtAuthResponse authenticate(UserLoginRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.email(), input.password())
        );
        User user = userRepository.findByEmail(input.email()).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found.")
        );
        String token = jwtService.generateToken((UserDetails) user);
        long tokenExpiration = jwtService.getExpirationTime();

        AccessToken accessToken = tokenRepository.findByUser(user);
        if (accessToken == null) {
            accessToken = AccessToken.builder()
                    .token(token)
                    .user(user)
                    .build();
        }
        tokenRepository.save(accessToken);

        return JwtAuthResponse.builder().token(token).expiresIn(tokenExpiration).build();
    }
}