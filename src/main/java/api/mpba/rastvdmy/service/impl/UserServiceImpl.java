package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.entity.User;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.UserRepository;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.UserService;
import api.mpba.rastvdmy.service.utils.CountryValidator;
import api.mpba.rastvdmy.service.utils.FinancialDataGenerator;
import api.mpba.rastvdmy.service.utils.GenerateAccessToken;
import api.mpba.rastvdmy.service.utils.UserDataValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl extends FinancialDataGenerator implements UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final GenerateAccessToken generateAccessToken;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           JwtService jwtService,
                           PasswordEncoder passwordEncoder,
                           RestTemplate restTemplate,
                           GenerateAccessToken generateAccessToken) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.generateAccessToken = generateAccessToken;
    }

    public List<User> getUsers(HttpServletRequest request) {
        validateUserData(request);
        List<User> users = userRepository.findAll();
        return users.stream().filter(this::decryptUserData).toList();
    }

    public Page<User> filterAndSortUsers(HttpServletRequest request, Pageable pageable) {
        validateUserData(request);
        Page<User> usersPage = userRepository.findAll(pageable);
        usersPage.forEach(this::decryptUserData);
        return usersPage;
    }

    public User getUserById(HttpServletRequest request, UUID id) {
        validateUserData(request);
        User user = userRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist."));
        decryptUserData(user);
        return user;
    }

    private User validateUserData(HttpServletRequest request) {
        User user = userRepository.findByEmail(request.getUserPrincipal().getName())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist."));

        if (user.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        return user;
    }

    public User getUser(HttpServletRequest request) {
        User user = validateUserData(request);
        decryptUserData(user);
        return user;
    }

    private boolean decryptUserData(User user) {
        try {
            SecretKey secretKey = EncryptionUtil.getSecretKey(); // Ensure this retrieves the same key used for encryption
            user.setDateOfBirth(EncryptionUtil.decrypt(user.getDateOfBirth(), secretKey));
            user.setPhoneNumber(EncryptionUtil.decrypt(user.getPhoneNumber(), secretKey));
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    public User updateUser(HttpServletRequest request, UserUpdateRequest userRequest) throws Exception {
        User user = validateUserData(request);

        // Validate user data
        UserDataValidator.isInvalidEmail(userRequest.email());
        UserDataValidator.isInvalidPassword(userRequest.password());
        UserDataValidator.isInvalidPhoneNumber(userRequest.phoneNumber());
        checkIfUserExistsByEmail(userRequest);
        checkIfUserExistsByPhoneNumber(userRequest);

        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));

        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encodedPhoneNumber = EncryptionUtil.encrypt(userRequest.phoneNumber(), secretKey);
        user.setPhoneNumber(encodedPhoneNumber);

        return userRepository.save(user);
    }

    private void checkIfUserExistsByEmail(UserUpdateRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with this email already exists."
            );
        }
    }

    private void checkIfUserExistsByPhoneNumber(UserUpdateRequest request) {
        List<User> users = userRepository.findAll();
        boolean isPhoneUsed = users.stream()
                .anyMatch(u -> checkIsNumberUsed(u, request));
        if (isPhoneUsed) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with phone number " + request.phoneNumber() + " already exists."
            );
        }
    }

    private boolean checkIsNumberUsed(User user, UserUpdateRequest request) {
        try {
            SecretKey secretKey = EncryptionUtil.getSecretKey();
            String decryptedPhoneNumber = EncryptionUtil.decrypt(user.getPhoneNumber(), secretKey);
            return decryptedPhoneNumber.equals(request.phoneNumber());
        } catch (Exception e) {
            return false;
        }
    }

    public User updateUserSpecificCredentials(HttpServletRequest request, UUID userId,
                                              @Valid AdminUpdateUserRequest userRequest) {
        validateUserData(request);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (user.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        UserDataValidator.isInvalidSurname(userRequest.surname());
        countryValidation(userRequest);

        user.setSurname(userRequest.surname());
        user.setCountryOrigin(userRequest.countryOfOrigin());

        return userRepository.save(user);
    }

    public String generateToken(User user) {
        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(user);
        return generatedToken.token();
    }

    private void countryValidation(AdminUpdateUserRequest request) {
        CountryValidator countryValidator = new CountryValidator(restTemplate);
        if (countryValidator.countryExists(request.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
    }

    public void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar) {
        User user = validateUserData(request);

        if (userAvatar.getContentType() == null || !userAvatar.getContentType().startsWith("image")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "File must be an image.");
        }
        user.setAvatar(userAvatar.getOriginalFilename());

        userRepository.save(user);
    }

    public void removeUserAvatar(HttpServletRequest request) {
        User user = validateUserData(request);
        user.setAvatar("https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1");

        userRepository.save(user);
    }

    public void updateUserRole(HttpServletRequest request, UUID userId) {
        validateUserData(request);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (user.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }

        if (getUserFromToken(request).equals(userId.toString())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot change your own role.");
        }

        switch (user.getRole()) {
            case ROLE_DEFAULT -> user.setRole(UserRole.ROLE_ADMIN);
            case ROLE_ADMIN -> user.setRole(UserRole.ROLE_DEFAULT);
        }
        userRepository.save(user);
    }

    public void updateUserStatus(HttpServletRequest request, UUID userId) {
        validateUserData(request);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (getUserFromToken(request).equals(userId.toString())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot change your own status.");
        }

        switch (user.getStatus()) {
            case STATUS_DEFAULT, STATUS_UNBLOCKED -> {
                user.setStatus(UserStatus.STATUS_BLOCKED);
                userRepository.save(user);
            }
            case STATUS_BLOCKED -> {
                user.setStatus(UserStatus.STATUS_UNBLOCKED);
                userRepository.save(user);
            }
        }
    }

    @Transactional
    public void deleteUser(HttpServletRequest request) {
        User user = validateUserData(request);

        if (!user.getBankIdentities().isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Make sure to delete all bank accounts first.");
        }

        userRepository.delete(user);
    }

    @Transactional
    public void deleteUserByEmail(HttpServletRequest request, UUID userId) {
        validateUserData(request);
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (getUserFromToken(request).equals(userId.toString())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot delete yourself.");
        }

        if (!user.getBankIdentities().isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Make sure to delete all bank accounts first.");
        }
        userRepository.delete(user);
    }

    public UserDetailsService userDetailsService() {
        return userData -> (UserDetails) userRepository.findByEmail(userData).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
    }

    private String getUserFromToken(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        return jwtService.extractSubject(token);
    }
}
