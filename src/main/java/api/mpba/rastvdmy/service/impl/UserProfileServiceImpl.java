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
import api.mpba.rastvdmy.service.generator.FinancialDataGenerator;
import api.mpba.rastvdmy.service.generator.GenerateAccessToken;
import api.mpba.rastvdmy.service.validator.UserDataValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for managing user profiles.
 * This class provides methods for retrieving, updating, and deleting user profiles,
 * as well as handling user authentication and authorization.
 * It also includes methods for encrypting and decrypting user data.
 */
@Slf4j
@Service
public class UserProfileServiceImpl extends FinancialDataGenerator implements UserProfileService {
    private final UserProfileRepository userProfileRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final GenerateAccessToken generateAccessToken;
    private final UserDataValidator userDataValidator;

    /**
     * Constructs a new UserProfileServiceImpl with the specified repository, JWT service,
     * password encoder, REST template, and access token generator.
     *
     * @param userProfileRepository The repository for user profiles.
     * @param jwtService            The service for handling JWT tokens.
     * @param passwordEncoder       The encoder for hashing passwords.
     * @param generateAccessToken   The generator for access tokens.
     * @param userDataValidator     The validator for user data.
     */
    @Autowired
    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                                  JwtService jwtService,
                                  PasswordEncoder passwordEncoder,
                                  GenerateAccessToken generateAccessToken, UserDataValidator userDataValidator) {
        this.userProfileRepository = userProfileRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.generateAccessToken = generateAccessToken;
        this.userDataValidator = userDataValidator;
    }

    /**
     * Retrieves all user profiles with decrypted data.
     *
     * @param request the HTTP request containing user authentication data
     * @return a list of user profiles
     */
    @Cacheable(value = "users")
    public List<UserProfile> getUsers(HttpServletRequest request) {
        validateUserData(request);
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        return userProfiles.stream().filter(this::decryptUserData).toList();
    }

    /**
     * Filters and sorts user profiles based on pagination.
     *
     * @param request  the HTTP request containing user authentication data
     * @param pageable pagination information
     * @return a paginated list of user profiles
     */
    @Cacheable(value = "users")
    public Page<UserProfile> filterAndSortUsers(HttpServletRequest request, Pageable pageable) {
        validateUserData(request);
        Page<UserProfile> usersPage = userProfileRepository.findAll(pageable);
        usersPage.forEach(this::decryptUserData);
        return usersPage;
    }

    /**
     * Retrieves a user profile based on the authenticated user.
     *
     * @param request the HTTP request containing user authentication data
     * @return the user profile of the authenticated user
     */
    @Cacheable(value = "users")
    public UserProfile getUser(HttpServletRequest request) {
        UserProfile userProfile = validateUserData(request);
        decryptUserData(userProfile);
        return userProfile;
    }

    /**
     * Retrieves a user profile by its unique ID.
     *
     * @param request the HTTP request containing user authentication data
     * @param id      the unique ID of the user profile
     * @return the user profile
     */
    @Cacheable(value = "users")
    public UserProfile getUserById(HttpServletRequest request, UUID id) {
        validateUserData(request);
        UserProfile userProfile = userProfileRepository.findById(id).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
        decryptUserData(userProfile);
        return userProfile;
    }

    /**
     * Validates user data from the HTTP request and retrieves the user profile.
     *
     * @param request the HTTP request containing user authentication data
     * @return the validated user profile
     */
    private UserProfile validateUserData(HttpServletRequest request) {
        return retrieveAndValidateUser(userProfileRepository.findByEmail(request.getUserPrincipal().getName()));
    }

    /**
     * Decrypts-sensitive data for a user profile.
     *
     * @param userProfile the user profile to decrypt
     * @return true if decryption was successful
     */
    private boolean decryptUserData(UserProfile userProfile) {
        try {
            // Ensure this retrieves the same key used for encryption
            SecretKey secretKey = EncryptionUtil.getSecretKey();

            userProfile.setDateOfBirth(EncryptionUtil.decrypt(userProfile.getDateOfBirth(), secretKey));
            userProfile.setPhoneNumber(EncryptionUtil.decrypt(userProfile.getPhoneNumber(), secretKey));
            return true;
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while decrypting account data.");
        }
    }

    /**
     * Updates the user profile with new information.
     *
     * @param request     the HTTP request containing user authentication data
     * @param userRequest the new user data
     * @return the updated user profile
     * @throws Exception if an error occurs during the update
     */
    @CachePut(value = "users", key = "#request.userPrincipal.name + '_update'")
    public UserProfile updateUser(HttpServletRequest request, UserUpdateRequest userRequest) throws Exception {
        UserProfile userProfile = validateUserData(request);

        // Validate user data
        userDataValidator.validateField("email", userRequest.email().trim());
        userDataValidator.validateField("password", userRequest.password().trim());
        userDataValidator.validateField("phoneNumber", userRequest.phoneNumber().trim());
        checkIfUserExistsByEmail(request, userRequest);
        checkIfUserExistsByPhoneNumber(request, userRequest);

        userProfile.setEmail(userRequest.email().trim());
        userProfile.setPassword(passwordEncoder.encode(userRequest.password().trim()));

        SecretKey secretKey = EncryptionUtil.getSecretKey();
        String encodedPhoneNumber = EncryptionUtil.encrypt(userRequest.phoneNumber().trim(), secretKey);
        userProfile.setPhoneNumber(encodedPhoneNumber);

        return userProfileRepository.save(userProfile);
    }

    /**
     * Checks if a user with the given email already exists.
     *
     * @param request     the HTTP request containing user authentication data
     * @param userRequest the user update request containing the new email
     * @throws ApplicationException if a user with the given email already exists
     */
    private void checkIfUserExistsByEmail(HttpServletRequest request, UserUpdateRequest userRequest) {
        UserProfile currentUser = validateUserData(request);
        if (!currentUser.getEmail().equals(userRequest.email()) &&
                userProfileRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with this email already exists."
            );
        }
    }

    /**
     * Checks if a user with the given phone number already exists.
     *
     * @param request     the HTTP request containing user authentication data
     * @param userRequest the user update request containing the new phone number
     * @throws ApplicationException if a user with the given phone number already exists
     */
    private void checkIfUserExistsByPhoneNumber(HttpServletRequest request, UserUpdateRequest userRequest) {
        UserProfile currentUser = validateUserData(request);
        List<UserProfile> userProfiles = userProfileRepository.findAll();
        boolean isPhoneUsed = userProfiles.stream()
                .anyMatch(u -> !u.getId().equals(currentUser.getId()) && checkIsNumberUsed(u, userRequest));
        if (isPhoneUsed) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST, "User with phone number " + userRequest.phoneNumber() + " already exists."
            );
        }
    }

    /**
     * Checks if the user already uses the phone number.
     *
     * @param userProfile the user profile
     * @param request     the user update request
     * @return true if the phone number is already used
     */
    private boolean checkIsNumberUsed(UserProfile userProfile, UserUpdateRequest request) {
        try {
            SecretKey secretKey = EncryptionUtil.getSecretKey();
            String decryptedPhoneNumber = EncryptionUtil.decrypt(userProfile.getPhoneNumber(), secretKey);
            return decryptedPhoneNumber.equals(request.phoneNumber());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Admin updates the user profile with new information.
     *
     * @param request     the HTTP request containing user authentication data
     * @param userId      the unique ID of the user profile
     * @param userRequest the new user data
     * @return the updated user profile
     */
    @CachePut(value = "users", key = "#request.userPrincipal.name + '_admin_update'")
    public UserProfile updateUserSpecificCredentials(HttpServletRequest request, UUID userId,
                                                     @Valid AdminUpdateUserRequest userRequest) {
        validateUserData(request);
        UserProfile userProfile = retrieveAndValidateUser(userProfileRepository.findById(userId));

        // Validate user data
        userDataValidator.validateField("name", userRequest.name().trim());
        userDataValidator.validateField("surname", userRequest.surname().trim());
        userDataValidator.validateField("country", userRequest.countryOfOrigin().trim());

        userProfile.setName(userRequest.name().trim());
        userProfile.setSurname(userRequest.surname().trim());
        userProfile.setCountryOfOrigin(userRequest.countryOfOrigin().trim());

        return userProfileRepository.save(userProfile);
    }

    /**
     * Generates an access token for the user profile.
     *
     * @param userProfile the user profile
     * @return the generated access token
     */
    public String generateToken(UserProfile userProfile) {
        GenerateAccessToken.TokenDetails generatedToken = generateAccessToken.generate(userProfile);
        return generatedToken.token();
    }

    /**
     * Uploads a user avatar image.
     *
     * @param request    the HTTP request containing user authentication data
     * @param userAvatar the user avatar image
     */
    @CachePut(value = "users", key = "#request.userPrincipal.name")
    public void uploadUserAvatar(HttpServletRequest request, MultipartFile userAvatar) {
        UserProfile userProfile = validateUserData(request);

        if (userAvatar.getContentType() == null || !userAvatar.getContentType().startsWith("image")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "File must be an image.");
        }
        userProfile.setAvatar(userAvatar.getOriginalFilename());

        userProfileRepository.save(userProfile);
    }

    /**
     * Removes the user avatar image.
     *
     * @param request the HTTP request containing user authentication data
     */
    @CachePut(value = "users", key = "#request.userPrincipal.name")
    public void removeUserAvatar(HttpServletRequest request) {
        UserProfile userProfile = validateUserData(request);
        userProfile.setAvatar("https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1");

        userProfileRepository.save(userProfile);
    }

    /**
     * Updates the user role.
     *
     * @param request the HTTP request containing user authentication data
     * @param userId  the unique ID of the user profile
     */
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

    /**
     * Updates the user status.
     *
     * @param request the HTTP request containing user authentication data
     * @param userId  the unique ID of the user profile
     */
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

    /**
     * Retrieves and validates the user profile.
     *
     * @param userProfileRepository the user profile repository
     * @return the user profile
     */
    private UserProfile retrieveAndValidateUser(Optional<UserProfile> userProfileRepository) {
        log.debug("User profile: {}", userProfileRepository);
        UserProfile userProfile = userProfileRepository.orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
        validateUserStatus(userProfile);
        return userProfile;
    }

    /**
     * Validates the user status.
     *
     * @param userProfile the user profile
     */
    private static void validateUserStatus(UserProfile userProfile) {
        if (userProfile.getStatus().equals(UserStatus.STATUS_BLOCKED)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
    }

    /**
     * Deletes the user profile.
     *
     * @param request the HTTP request containing user authentication data
     */
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

    /**
     * Deletes the user profile by email.
     *
     * @param request the HTTP request containing user authentication data
     * @param userId  the unique ID of the user profile
     */
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

    /**
     * Retrieves the user details service.
     *
     * @return the user details service
     */
    public UserDetailsService userDetailsService() {
        return userData -> (UserDetails) userProfileRepository.findByEmail(userData).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User does not exist.")
        );
    }

    /**
     * Extracts the user ID from the JWT token in the HTTP request.
     *
     * @param request the HTTP request containing the JWT token
     * @return the user ID extracted from the token
     */
    private String getUserFromToken(HttpServletRequest request) {
        final String token = jwtService.extractToken(request);
        return jwtService.extractSubject(token);
    }
}