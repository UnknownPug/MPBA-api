package api.mpba.rastvdmy.service.impl;

import api.mpba.rastvdmy.config.utils.EncryptionUtil;
import api.mpba.rastvdmy.dto.request.AdminUpdateUserRequest;
import api.mpba.rastvdmy.dto.request.UserUpdateRequest;
import api.mpba.rastvdmy.entity.UserProfile;
import api.mpba.rastvdmy.entity.enums.UserRole;
import api.mpba.rastvdmy.entity.enums.UserStatus;
import api.mpba.rastvdmy.exception.ApplicationException;
import api.mpba.rastvdmy.repository.UserProfileRepository;
import api.mpba.rastvdmy.service.JwtService;
import api.mpba.rastvdmy.service.UserProfileService;
import api.mpba.rastvdmy.service.utils.CountryValidator;
import api.mpba.rastvdmy.service.utils.FinancialDataGenerator;
import api.mpba.rastvdmy.service.utils.GenerateAccessToken;
import api.mpba.rastvdmy.service.utils.UserDataValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.Optional;
import java.util.UUID;

@Service
public class UserProfileServiceImpl extends FinancialDataGenerator implements UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final GenerateAccessToken generateAccessToken;

    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                                  JwtService jwtService,
                                  PasswordEncoder passwordEncoder,
                                  RestTemplate restTemplate,
                                  GenerateAccessToken generateAccessToken) {
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.generateAccessToken = generateAccessToken;
    }

    @Cacheable(value = "users")
    public List<UserProfile> getUsers(HttpServletRequest request) {
        validateUserData(request);
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        return userProfiles.stream().filter(this::decryptUserData).toList();
    }

    @Cacheable(value = "users")
    public Page<UserProfile> filterAndSortUsers(HttpServletRequest request, Pageable pageable) {
        validateUserData(request);
        Page<UserProfile> usersPage = userProfileRepository.findAll(pageable);
        usersPage.forEach(this::decryptUserData);
        return usersPage;
    }

    @Cacheable(value = "users")
    public UserProfile getUser(HttpServletRequest request) {
        UserProfile userProfile = validateUserData(request);
        decryptUserData(userProfile);
        return userProfile;
    }

    @Cacheable(value = "users")
    public UserProfile getUserById(HttpServletRequest request, UUID id) {
        validateUserData(request);
        UserProfile userProfile = userProfileRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
        decryptUserData(userProfile);
        return userProfile;
    }

    private UserProfile validateUserData(HttpServletRequest request) {
        return retrieveAndValidateUser(userProfileRepository.findByEmail(request.getUserPrincipal().getName()));
    }

    private boolean decryptUserData(UserProfile userProfile) {
        try {
            SecretKey secretKey = EncryptionUtil.getSecretKey(); // Ensure this retrieves the same key used for encryption
            userProfile.setDateOfBirth(EncryptionUtil.decrypt(userProfile.getDateOfBirth(), secretKey));
            userProfile.setPhoneNumber(EncryptionUtil.decrypt(userProfile.getPhoneNumber(), secretKey));
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    @CachePut(value = "users", key = "#request.userPrincipal.name + '_update'")
    public UserProfile updateUser(HttpServletRequest request, UserUpdateRequest userRequest) throws Exception {
        UserProfile userProfile = validateUserData(request);

        // Validate user data
        UserDataValidator.isInvalidEmail(userRequest.email());
        UserDataValidator.isInvalidPassword(userRequest.password());
        UserDataValidator.isInvalidPhoneNumber(userRequest.phoneNumber());
        checkIfUserExistsByEmail(userRequest);
        checkIfUserExistsByPhoneNumber(userRequest);

        userProfile.setEmail(userRequest.email());
        userProfile.setPassword(passwordEncoder.encode(userRequest.password()));

        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encodedPhoneNumber = EncryptionUtil.encrypt(userRequest.phoneNumber(), secretKey);
        userProfile.setPhoneNumber(encodedPhoneNumber);

        return userProfileRepository.save(userProfile);
    }

    private void checkIfUserExistsByEmail(UserUpdateRequest request) {
        if (userProfileRepository.findByEmail(request.email()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with this email already exists."
            );
        }
    }

    private void checkIfUserExistsByPhoneNumber(UserUpdateRequest request) {
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        boolean isPhoneUsed = userProfiles.stream()
                .anyMatch(u -> checkIsNumberUsed(u, request));
        if (isPhoneUsed) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with phone number " + request.phoneNumber() + " already exists."
            );
        }
    }

    private boolean checkIsNumberUsed(UserProfile userProfile, UserUpdateRequest request) {
        try {
            SecretKey secretKey = EncryptionUtil.getSecretKey();
            String decryptedPhoneNumber = EncryptionUtil.decrypt(userProfile.getPhoneNumber(), secretKey);
            return decryptedPhoneNumber.equals(request.phoneNumber());
        } catch (Exception e) {
            return false;
        }
    }

    @CachePut(value = "users", key = "#request.userPrincipal.name + '_admin_update'")
    public UserProfile updateUserSpecificCredentials(HttpServletRequest request, UUID userId,
                                                     @Valid AdminUpdateUserRequest userRequest) {
        validateUserData(request);

        UserProfile userProfile = retrieveAndValidateUser(userProfileRepository.findById(userId));

        UserDataValidator.isInvalidSurname(userRequest.surname());
        countryValidation(userRequest);

        userProfile.setSurname(userRequest.surname());
        userProfile.setCountryOrigin(userRequest.countryOfOrigin());

        return userProfileRepository.save(userProfile);
    }

    public String generateToken(UserProfile userProfile) {
        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(userProfile);
        return generatedToken.token();
    }

    private void countryValidation(AdminUpdateUserRequest request) {
        CountryValidator countryValidator = new CountryValidator(restTemplate);
        if (countryValidator.countryExists(request.countryOfOrigin())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country does not exist.");
        }
    }

    @CachePut(value = "users", key = "#request.userPrincipal.name")
    public void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar) {
        UserProfile userProfile = validateUserData(request);

        if (userAvatar.getContentType() == null || !userAvatar.getContentType().startsWith("image")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "File must be an image.");
        }
        userProfile.setAvatar(userAvatar.getOriginalFilename());

        userProfileRepository.save(userProfile);
    }

    @CachePut(value = "users", key = "#request.userPrincipal.name")
    public void removeUserAvatar(HttpServletRequest request) {
        UserProfile userProfile = validateUserData(request);
        userProfile.setAvatar("https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1");

        userProfileRepository.save(userProfile);
    }

    @CachePut(value = "users", key = "#request.userPrincipal.name")
    public void updateUserRole(HttpServletRequest request, UUID userId) {
        validateUserData(request);

        UserProfile userProfile = retrieveAndValidateUser(userProfileRepository.findById(userId));

        if (getUserFromToken(request).equals(userId.toString())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot change your own role.");
        }

        switch (userProfile.getRole()) {
            case ROLE_DEFAULT -> userProfile.setRole(UserRole.ROLE_ADMIN);
            case ROLE_ADMIN -> userProfile.setRole(UserRole.ROLE_DEFAULT);
        }
        userProfileRepository.save(userProfile);
    }

    @CachePut(value = "users", key = "#request.userPrincipal.name")
    public void updateUserStatus(HttpServletRequest request, UUID userId) {
        validateUserData(request);

        UserProfile userProfile = userProfileRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );

        if (getUserFromToken(request).equals(userId.toString())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot change your own status.");
        }

        switch (userProfile.getStatus()) {
            case STATUS_DEFAULT, STATUS_UNBLOCKED -> {
                userProfile.setStatus(UserStatus.STATUS_BLOCKED);
                userProfileRepository.save(userProfile);
            }
            case STATUS_BLOCKED -> {
                userProfile.setStatus(UserStatus.STATUS_UNBLOCKED);
                userProfileRepository.save(userProfile);
            }
        }
    }

    private UserProfile retrieveAndValidateUser(Optional<UserProfile> userProfileRepository) {
        UserProfile userProfile = userProfileRepository.orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
        validateUserStatus(userProfile);
        return userProfile;
    }

    private static void validateUserStatus(UserProfile userProfile) {
        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
    }

    @Transactional
    public void deleteUser(HttpServletRequest request) {
        UserProfile userProfile = validateUserData(request);

        if (!userProfile.getBankIdentities().isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Make sure to delete all bank accounts first.");
        }

        if (userProfile.getRole().equals(UserRole.ROLE_ADMIN)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Admin cannot deleted himself.");
        }

        userProfileRepository.delete(userProfile);
    }

    @Transactional
    public void deleteUserByEmail(HttpServletRequest request, UUID userId) {
        validateUserData(request);
        UserProfile userProfile = retrieveAndValidateUser(userProfileRepository.findById(userId));

        if (getUserFromToken(request).equals(userId.toString())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "You cannot delete yourself.");
        }

        if (!userProfile.getBankIdentities().isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Make sure to delete all bank accounts first.");
        }
        userProfileRepository.delete(userProfile);
    }

    public UserDetailsService userDetailsService() {
        return userData -> (UserDetails) userProfileRepository.findByEmail(userData).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
    }

    private String getUserFromToken(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        return jwtService.extractSubject(token);
    }
}