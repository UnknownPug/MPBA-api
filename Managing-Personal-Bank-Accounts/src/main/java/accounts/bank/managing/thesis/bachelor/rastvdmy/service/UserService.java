package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.*;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.CurrencyDataRepository;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@CacheConfig(cacheNames = {"users"})
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyDataRepository currencyDataRepository;
    private final CardService cardService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CurrencyDataRepository currencyDataRepository, CardService cardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyDataRepository = currencyDataRepository;
        this.cardService = cardService;
    }

    @Cacheable
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Cacheable
    public Page<User> filterAndSortUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Cacheable(key = "#userId")
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
    }

    @CacheEvict(allEntries = true)
    public User createUser(String name, String surname, LocalDate dateOfBirth, String countryOfOrigin,
                           String email, String password, String phoneNumber) {
        if (name.isEmpty() || surname.isEmpty() || countryOfOrigin.isEmpty() ||
                email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty()) {
            throw new ApplicationException(HttpStatus.NO_CONTENT, "All user fields must be filled.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number " + phoneNumber + " is unavailable.");
        }
        // Creating user with default role and status
        User user = new User();
        if (!isValidName(name)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Name should be between 2 and 10 characters.");
        }
        user.setName(HtmlUtils.htmlEscape(name));
        if (!isValidSurname(surname)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Surname should be between 2 and 15 characters.");
        }
        user.setSurname(HtmlUtils.htmlEscape(surname));
        if (dateOfBirth.isAfter(LocalDate.of(LocalDate.now().getYear() - 18,
                LocalDate.MAX.getMonth(),
                LocalDate.MAX.getDayOfMonth())) ||
                dateOfBirth.isBefore(LocalDate.of(LocalDate.now().getYear() - 100,
                        LocalDate.MIN.getMonth(),
                        LocalDate.MIN.getDayOfMonth()))) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "The age of the user can be between 18 and 100.");
        }
        user.setDateOfBirth(dateOfBirth);
        if (countryExists(HtmlUtils.htmlEscape(countryOfOrigin))) {
            user.setCountryOrigin(countryOfOrigin);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Country " + countryOfOrigin + " does not exist.");
        }
        validateUserData(email, password, phoneNumber, user);
        List<CurrencyData> currencyData = currencyDataRepository.findAll();
        user.setCurrencyData(currencyData);
        // Save the user and get the saved instance. Then create a card for the user
        User savedUser = userRepository.save(user);
        Card card = cardService.createCard(savedUser.getId(), Currency.CZK.toString(), CardType.VISA.toString());
        savedUser.getCards().add(card);
        return userRepository.save(savedUser);
    }

    private boolean countryExists(String countryName) {
        final String url = "https://restcountries.com/v3.1/all?fields=name";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.getBody());
                if (rootNode.isArray()) {
                    for (JsonNode node : rootNode) {
                        JsonNode nameNode = node.get("name");
                        if (nameNode != null && nameNode.get("common") != null) {
                            String commonName = nameNode.get("common").asText();
                            if (commonName.equalsIgnoreCase(countryName)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (RestClientException | IOException e) {
            e.getCause();
        }
        return false;
    }

    @CacheEvict(allEntries = true)
    public void updateUserById(Long userId, String email, String password, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (email == null || password == null || phoneNumber == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "All user fields must be filled.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number " + phoneNumber + " is unavailable.");
        }
        if (Objects.equals(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered password is the same as the old one.");
        }
        validateUserData(email, password, phoneNumber, user);
        userRepository.save(user);
    }

    private void validateUserData(String email, String password, String phoneNumber, User user) {
        if (isInvalidEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email must contain valid tags.");
        }
        user.setEmail(HtmlUtils.htmlEscape(email));
        if (isInvalidPassword(password)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one uppercase letter and one number or symbol.");
        }
        user.setPassword(HtmlUtils.htmlEscape(password));
        user.encodePassword(passwordEncoder);
        if (isInvalidPhoneNumber(HtmlUtils.htmlEscape(phoneNumber))) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number should be in international format.");
        }
        user.setPhoneNumber(phoneNumber);
    }

    @CacheEvict(allEntries = true)
    public void uploadUserAvatar(Long userId, MultipartFile userAvatar) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (userAvatar.getContentType() == null || !userAvatar.getContentType().startsWith("image")) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "File must be an image.");
        }
        user.setAvatar(userAvatar.getOriginalFilename());
        userRepository.save(user);
    }

    @CacheEvict(allEntries = true)
    public void updateUserEmailById(Long userId, String email) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (email == null) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email must be filled.");
        }
        if (Objects.equals(email, user.getEmail())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered email is the same as the old one.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email " + email + " is unavailable.");
        }
        if (isInvalidEmail(email)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Email must contain valid tags.");
        }
        user.setEmail(HtmlUtils.htmlEscape(email));
        userRepository.save(user);
    }

    @CacheEvict(allEntries = true)
    public void updateUserPasswordById(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (password.isEmpty()) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Password must be filled.");
        }
        if (Objects.equals(password, user.getPassword())) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Entered password is the same as the old one.");
        }
        if (isInvalidPassword(password)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least one uppercase letter and one number or symbol.");
        }
        user.setPassword(HtmlUtils.htmlEscape(password));
        user.encodePassword(passwordEncoder);
        userRepository.save(user);
    }

    @CacheEvict(allEntries = true)
    public void updateUserRoleById(Long userId, String role) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        switch (role.toLowerCase()) {
            case "admin" -> user.setUserRole(UserRole.ROLE_ADMIN);
            case "moderator" -> user.setUserRole(UserRole.ROLE_MODERATOR);
            default -> user.setUserRole(UserRole.ROLE_USER);
        }
        userRepository.save(user);
    }

    @CacheEvict(allEntries = true)
    public void updateUserStatusById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
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

    @CacheEvict(allEntries = true)
    public void updateUserVisibilityById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        switch (user.getVisibility()) {
            case STATUS_ONLINE -> {
                user.setVisibility(UserVisibility.STATUS_OFFLINE);
                userRepository.save(user);
            }
            case STATUS_OFFLINE -> {
                user.setVisibility(UserVisibility.STATUS_ONLINE);
                userRepository.save(user);
            }
        }

    }

    @CacheEvict(allEntries = true)
    public void updateUserPhoneNumberById(Long userId, String phoneNumber) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id: " + userId + " not found.")
        );
        if (user.getStatus() == UserStatus.STATUS_BLOCKED) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Operation is forbidden. User is blocked.");
        }
        if (phoneNumber == null || Objects.equals(phoneNumber, user.getPhoneNumber())) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Phone number " + phoneNumber + " is unavailable.");
        }
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "Phone number " + phoneNumber + " is unavailable.");
        }
        if (isInvalidPhoneNumber(phoneNumber)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Phone number should be in international format.");
        }
        user.setPhoneNumber(HtmlUtils.htmlEscape(phoneNumber));
        userRepository.save(user);
    }

    @CacheEvict(allEntries = true)
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "User with id " + userId + " not found.")
        );
        if (user.getCards().isEmpty()) {
            userRepository.delete(user);
        } else {
            throw new ApplicationException(HttpStatus.BAD_REQUEST,
                    "User with id " + userId + " has cards. Delete the cards to remove user.");
        }
    }

    private boolean isValidName(String name) {
        return name != null && name.length() >= 2 /*&& name.length() <= 10*/;
    }

    private boolean isValidSurname(String surname) {
        return surname != null && surname.length() >= 2 && surname.length() <= 15;
    }

    private boolean isInvalidEmail(String email) {
        return !EmailValidator.getInstance().isValid(email);
    }

    private boolean isInvalidPassword(String password) {
        // Password validation logic
        return password == null || !password.matches("^(?=.*[A-Z])(?=.*[0-9\\W]).{8,20}$");
    }

    private boolean isInvalidPhoneNumber(String phoneNumber) {
        // Phone number validation logic
        return phoneNumber == null || !phoneNumber.matches("^\\+\\d{1,3}\\d{9,15}$");
    }
}
